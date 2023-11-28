package org.radarbase.schema.registration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.admin.ListTopicsOptions
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.radarbase.kotlin.coroutines.suspendGet
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.ToolConfig
import org.radarbase.schema.specification.config.TopicConfig
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Registers Kafka topics with Zookeeper.
 */
class KafkaTopics(
    private val toolConfig: ToolConfig,
) : TopicRegistrar {
    private var initialized = false
    override lateinit var topics: Set<String>

    private val adminClient: AdminClient = AdminClient.create(toolConfig.kafka)

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    override suspend fun initialize(brokers: Int) {
        initialize(brokers, 20)
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism, retrying with sleep
     * up to the supplied numTries on failures. The sleep time is doubled every retry
     * iteration until the {@value #MAX_SLEEP} is reached which then takes precedence.
     *
     * @param brokers number of brokers to wait for.
     * @param numTries Number of times to retry in case of failure.
     */
    override suspend fun initialize(brokers: Int, numTries: Int) {
        val numBrokers = retryFlow(2.seconds, MAX_SLEEP)
            .take(numTries)
            .map { sleep ->
                try {
                    withContext(Dispatchers.IO) {
                        adminClient.describeCluster()
                            .nodes()
                            .suspendGet(sleep)
                            .size
                    }
                } catch (ex: InterruptedException) {
                    logger.error("Refreshing topics interrupted")
                    throw ex
                } catch (ex: TimeoutException) {
                    logger.error(
                        "Failed to connect to bootstrap server {} within {} seconds",
                        kafkaProperties[BOOTSTRAP_SERVERS_CONFIG],
                        sleep,
                    )
                    0
                } catch (ex: Throwable) {
                    logger.error(
                        "Failed to connect to bootstrap server {}",
                        kafkaProperties[BOOTSTRAP_SERVERS_CONFIG],
                        ex.cause,
                    )
                    0
                }
            }
            .firstOrNull { numBrokers ->
                if (numBrokers >= brokers) {
                    true
                } else {
                    logger.warn("Only {} out of {} Kafka brokers available.", numBrokers, brokers)
                    false
                }
            }

        initialized = numBrokers != null
        check(initialized) { "Brokers not available." }
        check(refreshTopics()) { "Topics not available." }
    }

    override fun ensureInitialized() {
        check(initialized) { "Manager is not initialized yet" }
    }

    override suspend fun createTopics(
        catalogue: SourceCatalogue,
        partitions: Int,
        replication: Short,
        topic: String?,
        match: String?,
    ): Int {
        val pattern = TopicRegistrar.matchTopic(topic, match)
        return if (pattern == null) {
            if (createTopics(catalogue, partitions, replication)) 0 else 1
        } else {
            val topicNames = topicNames(catalogue)
                .filter { s -> pattern.matcher(s).find() }
                .collect(Collectors.toList())
            if (topicNames.isEmpty()) {
                logger.error(
                    "Topic {} does not match a known topic." +
                        " Find the list of acceptable topics" +
                        " with the `radar-schemas-tools list` command. Aborting.",
                    pattern,
                )
                return 1
            }
            if (createTopics(topicNames.stream(), partitions, replication)) 0 else 1
        }
    }

    private fun topicNames(catalogue: SourceCatalogue): Stream<String> {
        return Stream.concat(
            catalogue.topicNames,
            toolConfig.topics.keys.stream(),
        ).filter { t -> toolConfig.topics[t]?.enabled != false }
    }

    /**
     * Create all topics in a catalogue.
     *
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    private suspend fun createTopics(
        catalogue: SourceCatalogue,
        partitions: Int,
        replication: Short,
    ): Boolean {
        ensureInitialized()
        return createTopics(topicNames(catalogue), partitions, replication)
    }

    override suspend fun createTopics(
        topics: Stream<String>,
        partitions: Int,
        replication: Short,
    ): Boolean {
        ensureInitialized()
        return try {
            refreshTopics()
            logger.info("Creating topics. Topics marked with [*] already exist.")
            val newTopics = topics
                .sorted()
                .distinct()
                .filter { t: String ->
                    if (this.topics.contains(t)) {
                        logger.info("[*] {}", t)
                        return@filter false
                    } else {
                        logger.info("[ ] {}", t)
                        return@filter true
                    }
                }
                .map { t ->
                    val topicConfig = toolConfig.topics[t]
                        ?: TopicConfig()
                    NewTopic(
                        t,
                        topicConfig.partitions ?: partitions,
                        topicConfig.replicationFactor ?: replication,
                    ).configs(topicConfig.properties)
                }
                .collect(Collectors.toList())

            if (newTopics.isNotEmpty()) {
                kafkaClient
                    .createTopics(newTopics)
                    .all()
                    .suspendGet()
                logger.info("Created {} topics. Requesting to refresh topics", newTopics.size)
                refreshTopics()
            } else {
                logger.info("All of the topics are already created.")
            }
            true
        } catch (ex: Exception) {
            logger.error("Failed to create topics {}", ex.toString())
            false
        }
    }

    @Throws(InterruptedException::class)
    override suspend fun refreshTopics(): Boolean {
        ensureInitialized()
        logger.info("Waiting for topics to become available.")

        topics = emptySet()
        val opts = ListTopicsOptions().apply {
            listInternal(true)
        }

        topics = retryFlow(2.seconds, MAX_SLEEP)
            .take(10)
            .map { sleep ->
                try {
                    kafkaClient
                        .listTopics(opts)
                        .names()
                        .suspendGet(sleep)
                } catch (ex: TimeoutException) {
                    logger.error("Failed to list topics within {} seconds", sleep)
                    emptySet()
                } catch (ex: InterruptedException) {
                    logger.error("Refreshing topics interrupted")
                    throw ex
                } catch (ex: Throwable) {
                    logger.error("Failed to list topics from brokers: {}", ex.cause.toString())
                    emptySet()
                }
            }
            .firstOrNull { it.isNotEmpty() }
            ?: emptySet()

        return topics.isNotEmpty()
    }

    override fun close() {
        adminClient.close()
    }

    /**
     * Get current number of Kafka brokers according to Zookeeper.
     *
     * @return number of Kafka brokers
     */
    suspend fun numberOfBrokers(): Int {
        return adminClient.describeCluster()
            .nodes()
            .suspendGet()
            .size
    }

    override val kafkaClient: Admin
        get() {
            ensureInitialized()
            return adminClient
        }

    override val kafkaProperties: Map<String, *>
        get() = toolConfig.kafka

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaTopics::class.java)
        private val MAX_SLEEP = 32.seconds

        @JvmStatic
        fun ToolConfig.configureKafka(
            bootstrapServers: String?,
        ): ToolConfig = if (bootstrapServers.isNullOrEmpty()) {
            check(BOOTSTRAP_SERVERS_CONFIG in kafka) {
                "Cannot configure Kafka without $BOOTSTRAP_SERVERS_CONFIG property"
            }
            this
        } else {
            copy(
                kafka = buildMap {
                    putAll(kafka)
                    put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                    System.getenv("KAFKA_SASL_JAAS_CONFIG")?.let {
                        put(SASL_JAAS_CONFIG, it)
                    }
                },
            )
        }

        fun retryFlow(
            startSleep: kotlin.time.Duration,
            maxSleep: kotlin.time.Duration,
        ): Flow<kotlin.time.Duration> = flow {
            var sleep = startSleep

            while (true) {
                // All computation for the sequence will be done in yield. It should be excluded
                // from sleep.
                val endTime = TimeSource.Monotonic.markNow() + sleep
                emit(sleep)
                sleepUntil(endTime) { timeUntil ->
                    logger.info("Waiting {} seconds to retry", timeUntil)
                }
                if (sleep < maxSleep) {
                    sleep = (sleep * 2).coerceAtMost(maxSleep)
                }
            }
        }

        private suspend fun sleepUntil(time: TimeMark, beforeSleep: (kotlin.time.Duration) -> Unit) {
            val timeUntil = -time.elapsedNow()
            if (timeUntil.isPositive()) {
                beforeSleep(timeUntil)
                delay(timeUntil)
            }
        }
    }
}
