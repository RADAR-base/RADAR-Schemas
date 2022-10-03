package org.radarbase.schema.registration

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.admin.ListTopicsOptions
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.radarbase.schema.specification.SourceCatalogue
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Stream

/**
 * Registers Kafka topics with Zookeeper.
 */
class KafkaTopics(
    private val toolConfig: ToolConfig,
) : TopicRegistrar {
    private var initialized = false
    private var topics: Set<String>? = null
    private val adminClient: AdminClient = AdminClient.create(toolConfig.kafka)

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    @Throws(InterruptedException::class)
    override fun initialize(brokers: Int) {
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
    override fun initialize(brokers: Int, numTries: Int) {
        val numBrokers = retrySequence(Duration.ofSeconds(2), MAX_SLEEP)
            .take(numTries)
            .map { sleep ->
                try {
                    adminClient.describeCluster()
                        .nodes()
                        .get(sleep.toSeconds(), TimeUnit.SECONDS)
                        .size
                } catch (ex: ExecutionException) {
                    logger.error("Failed to connect to bootstrap server {}",
                        kafkaProperties[BOOTSTRAP_SERVERS_CONFIG], ex.cause)
                    0
                } catch (ex: TimeoutException) {
                    logger.error("Failed to connect to bootstrap server {} within {} seconds",
                        kafkaProperties[BOOTSTRAP_SERVERS_CONFIG], sleep)
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

    override fun createTopics(
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
                .toList()
            if (topicNames.isEmpty()) {
                logger.error("Topic {} does not match a known topic."
                    + " Find the list of acceptable topics"
                    + " with the `radar-schemas-tools list` command. Aborting.", pattern)
                return 1
            }
            if (createTopics(topicNames.stream(), partitions, replication)) 0 else 1
        }
    }

    private fun topicNames(catalogue: SourceCatalogue): Stream<String> {
        return Stream.concat(
            catalogue.topicNames,
            toolConfig.topics.entries.stream()
                .filter { (_, c) -> c.enabled }
                .map { (t, _) -> t }
        )
    }

    /**
     * Create all topics in a catalogue.
     *
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    private fun createTopics(
        catalogue: SourceCatalogue,
        partitions: Int,
        replication: Short
    ): Boolean {
        ensureInitialized()
        return createTopics(topicNames(catalogue), partitions, replication)
    }

    override fun createTopics(
        topicsToCreate: Stream<String>,
        partitions: Int,
        replication: Short
    ): Boolean {
        ensureInitialized()
        return try {
            refreshTopics()
            logger.info("Creating topics. Topics marked with [*] already exist.")
            val newTopics = topicsToCreate
                .sorted()
                .distinct()
                .filter { t: String ->
                    if (topics?.contains(t) == true) {
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
                .toList()

            if (newTopics.isNotEmpty()) {
                kafkaClient
                    .createTopics(newTopics)
                    .all()
                    .get()
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
    override fun refreshTopics(): Boolean {
        ensureInitialized()
        logger.info("Waiting for topics to become available.")

        topics = null
        val opts = ListTopicsOptions().apply {
            listInternal(true)
        }

        topics = retrySequence(Duration.ofSeconds(2), MAX_SLEEP)
            .take(10)
            .map { sleep ->
                try {
                    kafkaClient
                        .listTopics(opts)
                        .names()
                        .get(sleep.toSeconds(), TimeUnit.SECONDS)
                } catch (ex: ExecutionException) {
                    logger.error("Failed to list topics from brokers: {}", ex.cause.toString())
                    emptySet()
                } catch (ex: TimeoutException) {
                    logger.error("Failed to list topics within {} seconds", sleep)
                    emptySet()
                }
            }
            .firstOrNull { it.isNotEmpty() }

        return topics != null
    }

    override fun getTopics(): Set<String> {
        ensureInitialized()
        return Collections.unmodifiableSet(checkNotNull(topics) {
            "Topics were not properly initialized"
        })
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
    @get:Throws(ExecutionException::class,
        InterruptedException::class)
    val numberOfBrokers: Int
        get() = adminClient.describeCluster()
            .nodes()
            .get()
            .size

    override fun getKafkaClient(): AdminClient {
        ensureInitialized()
        return adminClient
    }

    override fun getKafkaProperties(): Map<String, Any?> = toolConfig.kafka

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaTopics::class.java)
        private val MAX_SLEEP = Duration.ofSeconds(32)
        @JvmStatic
        fun ToolConfig.configureKafka(
            bootstrapServers: String?
        ): ToolConfig = if (bootstrapServers.isNullOrEmpty()) {
            check(BOOTSTRAP_SERVERS_CONFIG in kafka) {
                "Cannot configure Kafka without $BOOTSTRAP_SERVERS_CONFIG property"
            }
            this
        } else {
            copy(kafka = buildMap {
                putAll(kafka)
                put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                System.getenv("KAFKA_SASL_JAAS_CONFIG")?.let {
                    put(SASL_JAAS_CONFIG, it)
                }
            })
        }

        fun retrySequence(startSleep: Duration, maxSleep: Duration): Sequence<Duration> {
            return generateSequence(Pair(Duration.ZERO, Instant.now())) { (sleep, previousTime) ->
                val nextSleep = if (sleep == Duration.ZERO) {
                    startSleep
                } else {
                    val timeToSleep = Duration.between(previousTime + sleep, Instant.now())
                    if (!timeToSleep.isNegative) {
                        val sleepMillis = timeToSleep.toMillis()
                        logger.info("Waiting {} seconds to retry", (sleepMillis / 100) / 10.0)
                        Thread.sleep(sleepMillis)
                    }
                    if (sleep < maxSleep) {
                        sleep.multipliedBy(2L).coerceAtMost(maxSleep)
                    } else sleep
                }
                Pair(nextSleep, Instant.now())
            }.map { (sleep, _) -> sleep }
        }
    }
}
