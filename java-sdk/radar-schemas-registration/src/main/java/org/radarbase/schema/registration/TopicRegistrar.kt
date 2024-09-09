package org.radarbase.schema.registration

import org.apache.kafka.clients.admin.Admin
import org.radarbase.schema.specification.SourceCatalogue
import java.io.Closeable
import java.util.regex.Pattern
import java.util.stream.Stream

/**
 * Registers topic on configured Kafka environment.
 */
interface TopicRegistrar : Closeable {
    /**
     * list of topics from Kafka.
     */
    val topics: Set<String>

    /** Kafka Admin client.  */
    val kafkaClient: Admin

    /** Kafka Admin properties.  */
    val kafkaProperties: Map<String, *>

    /**
     * Create all topics in a catalogue based on pattern provided.
     *
     * @param catalogue source catalogue to extract topic names from.
     * @param partitions number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @param topic Topic name if registering the schemas only for topic.
     * @param match Regex string to register schemas only for topics that match the pattern.
     * @return 0 if execution was successful. 1 otherwise.
     */
    suspend fun createTopics(
        catalogue: SourceCatalogue,
        partitions: Int,
        replication: Short,
        topic: String?,
        match: String?,
    ): Int

    /**
     * Create a single topic.
     *
     * @param topics names of the topic to create.
     * @param partitions number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @return whether the topic was registered.
     */
    suspend fun createTopics(topics: Stream<String>, partitions: Int, replication: Short): Boolean

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws IllegalStateException when the brokers are not ready.
     */
    suspend fun initialize(brokers: Int)

    suspend fun initialize(brokers: Int, numTries: Int)

    /**
     * Ensures this topicRegistrar instance is initialized for use.
     */
    fun ensureInitialized()

    /**
     * Updates the list of topics from Kafka.
     *
     * @return `true` if the update succeeded, `false` otherwise.
     * @throws InterruptedException if the request was interrupted.
     */
    @Throws(InterruptedException::class)
    suspend fun refreshTopics(): Boolean

    companion object {
        /**
         * Create a pattern to match given topic. If the exact match is non-null, it is returned as an
         * exact match, otherwise if regex is non-null, it is used, and otherwise `null` is
         * returned.
         *
         * @param exact string that should be exactly matched.
         * @param regex string that should be matched as a regex.
         * @return pattern or `null` if both exact and regex are `null`.
         */
        fun matchTopic(exact: String?, regex: String?): Pattern? {
            return if (exact != null) {
                Pattern.compile("^" + Pattern.quote(exact) + "$")
            } else if (regex != null) {
                Pattern.compile(regex)
            } else {
                null
            }
        }
    }
}
