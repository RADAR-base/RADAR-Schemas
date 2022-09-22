package org.radarbase.schema.registration

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.ListTopicsOptions
import org.apache.kafka.clients.admin.NewTopic
import org.radarbase.schema.specification.SourceCatalogue
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Stream
import kotlin.io.path.inputStream

/**
 * Registers Kafka topics with Zookeeper.
 */
class KafkaTopics(
    private val kafkaProperties: Map<String, Any>,
) : TopicRegistrar {
    private var initialized = false
    private var topics: Set<String>? = null
    private val adminClient: AdminClient = AdminClient.create(kafkaProperties)

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
                        kafkaProperties[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG], ex.cause)
                    0
                } catch (ex: TimeoutException) {
                    logger.error("Failed to connect to bootstrap server {} within {} seconds",
                        kafkaProperties[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG], sleep)
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
        topic: String, match: String
    ): Int {
        val pattern = TopicRegistrar.matchTopic(topic, match)
        return if (pattern == null) {
            if (createTopics(catalogue, partitions, replication)) 0 else 1
        } else {
            val topicNames = catalogue.topicNames
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
        return createTopics(catalogue.topicNames, partitions, replication)
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
                .map { t -> NewTopic(t, partitions, replication) }
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
        checkNotNull(topics)
        return Collections.unmodifiableSet(checkNotNull(topics))
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

    override fun getKafkaProperties(): Map<String, Any?> {
        return kafkaProperties
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaTopics::class.java)
        private val MAX_SLEEP = Duration.ofSeconds(32)
        @JvmStatic
        @Throws(IOException::class)
        fun loadConfig(
            configFile: String?,
            bootstrapServers: String?
        ): Map<String, Any?> {
            val kafkaConfig: MutableMap<String, Any?> = LinkedHashMap<String, Any?>()

            if (!configFile.isNullOrEmpty()) {
                val cfg = Properties()
                Paths.get(configFile).inputStream().use { cfg.load(it) }
                cfg.forEach { (k, v) ->
                    kafkaConfig[k as String] = v
                }
            }
            if (!bootstrapServers.isNullOrEmpty()) {
                kafkaConfig[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
            }
            checkNotNull(kafkaConfig[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG]) {
                ("Cannot configure Kafka without "
                    + AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG + " property")
            }
            return kafkaConfig
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
                    sleep.multipliedBy(2L).coerceAtMost(maxSleep)
                }
                Pair(nextSleep, Instant.now())
            }.map { (sleep, _) -> sleep }
        }
    }
}
