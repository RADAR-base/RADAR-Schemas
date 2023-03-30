package org.radarbase.schema.registration

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Registers Kafka topics with Zookeeper.
 */
class KafkaTopics(
    private val toolConfig: ToolConfig,
) : TopicRegistrar {
    private var initialized = false
    private var _topics: Set<String>? = null

    private val adminClient: AdminClient = AdminClient.create(toolConfig.kafka)

    override val kafkaProperties: Map<String, Any?> = toolConfig.kafka.toMap()

    override val topics: Set<String>
        get() {
            ensureInitialized()
            return checkNotNull(_topics) {
                "Topics were not properly initialized"
            }.toSet()
        }

    override val kafkaClient: Admin
        get() {
            ensureInitialized()
            return adminClient
        }

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    @Throws(InterruptedException::class)
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
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    @Throws(InterruptedException::class)
    override suspend fun initialize(brokers: Int, numTries: Int) = coroutineScope {
        val numBrokers = retryDelayFlow(2.seconds, MAX_SLEEP)
            .take(numTries)
            .map { sleep ->
                try {
                    withContext(Dispatchers.IO) {
                        adminClient.describeCluster()
                            .nodes()
                            .suspendGet(sleep.coerceAtLeast(250.milliseconds))
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
            if (topicNames.isEmpty()) {
                logger.error(
                    "Topic {} does not match a known topic." +
                        " Find the list of acceptable topics" +
                        " with the `radar-schemas-tools list` command. Aborting.",
                    pattern,
                )
                return 1
            }
            if (createTopics(topicNames, partitions, replication)) 0 else 1
        }
    }

    private fun topicNames(catalogue: SourceCatalogue): List<String> {
        return Stream.concat(
            catalogue.topicNames,
            toolConfig.topics.keys.stream(),
        )
            .filter { t -> toolConfig.topics[t]?.enabled != false }
            .collect(Collectors.toList())
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
        return createTopics(
            topicNames(catalogue),
            partitions,
            replication,
        )
    }

    override suspend fun createTopics(
        topicsToCreate: List<String>,
        partitions: Int,
        replication: Short,
    ): Boolean {
        ensureInitialized()
        return try {
            refreshTopics()
            logger.info("Creating topics. Topics marked with [*] already exist.")
            val newTopics = topicsToCreate
                .toSortedSet()
                .filter { t: String ->
                    if (_topics?.contains(t) == true) {
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
                    ).apply {
                        configs(topicConfig.properties)
                    }
                }

            if (newTopics.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    kafkaClient
                        .createTopics(newTopics)
                        .all()
                        .suspendGet()
                }
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

        _topics = null
        val opts = ListTopicsOptions().apply {
            listInternal(true)
        }

        _topics = retryDelayFlow(2.seconds, MAX_SLEEP)
            .take(10)
            .map { sleep ->
                try {
                    withContext(Dispatchers.IO) {
                        kafkaClient
                            .listTopics(opts)
                            .names()
                            .suspendGet(sleep.coerceAtLeast(250.milliseconds))
                    }
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

        return _topics != null
    }

    override fun close() {
        adminClient.close()
    }

    /**
     * Get current number of Kafka brokers according to Zookeeper.
     *
     * @return number of Kafka brokers
     * @throws ExecutionException if kafka cannot connect
     * @throws InterruptedException if the query is interrupted.
     */
    suspend fun numberOfBrokers(
        coroutineContext: CoroutineContext = Dispatchers.IO,
    ): Int = withContext(coroutineContext) {
        adminClient.describeCluster()
            .nodes()
            .suspendGet()
            .size
    }

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

        @OptIn(ExperimentalTime::class)
        fun retryDelayFlow(
            startSleep: Duration,
            maxSleep: Duration,
        ): Flow<Duration> = flow {
            var sleep = startSleep
            var nextTime = TimeSource.Monotonic.markNow()

            while (currentCoroutineContext().isActive) {
                // All computation for the sequence will be done in yield. It should be excluded
                // from sleep.
                nextTime += sleep
                val timeToWait = -nextTime.elapsedNow()
                emit(timeToWait)
                if (timeToWait.isPositive()) {
                    logger.info("Waiting {} to retry", timeToWait)
                    delay(timeToWait)
                }
                if (sleep < maxSleep) {
                    sleep = (sleep * 2).coerceAtMost(maxSleep)
                }
            }
        }
    }
}
