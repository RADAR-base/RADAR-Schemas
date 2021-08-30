package org.radarbase.schema.registration;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.apache.kafka.clients.admin.Admin;
import org.radarbase.schema.specification.SourceCatalogue;

/**
 * Registers topic on configured Kafka environment.
 */
public interface TopicRegistrar extends Closeable {

    /**
     * Create a pattern to match given topic. If the exact match is non-null, it is returned as an
     * exact match, otherwise if regex is non-null, it is used, and otherwise {@code null} is
     * returned.
     *
     * @param exact string that should be exactly matched.
     * @param regex string that should be matched as a regex.
     * @return pattern or {@code null} if both exact and regex are {@code null}.
     */
    static Pattern matchTopic(String exact, String regex) {
        if (exact != null) {
            return Pattern.compile("^" + Pattern.quote(exact) + "$");
        } else if (regex != null) {
            return Pattern.compile(regex);
        } else {
            return null;
        }
    }

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
    int createTopics(@NotNull SourceCatalogue catalogue, int partitions, short replication,
            String topic, String match);

    /**
     * Create a single topic.
     *
     * @param topics names of the topic to create.
     * @param partitions number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @return whether the topic was registered.
     */
    boolean createTopics(Stream<String> topics, int partitions, short replication);

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws InterruptedException when waiting for the brokers is interrupted.
     * @throws IllegalStateException when the brokers are not ready.
     */
    void initialize(int brokers) throws InterruptedException;

    void initialize(int brokers, int numTries) throws InterruptedException;

    /**
     * Ensures this topicRegistrar instance is initialized for use.
     */
    void ensureInitialized();

    /**
     * Updates the list of topics from Kafka.
     *
     * @return {@code true} if the update succeeded, {@code false} otherwise.
     * @throws InterruptedException if the request was interrupted.
     */
    boolean refreshTopics() throws InterruptedException;

    /**
     * Returns the list of topics from Kafka.
     *
     * @return {@code List<String>} list of topics.
     */
    Set<String> getTopics();

    /** Kafka Admin client. */
    Admin getKafkaClient();

    /** Kafka Admin properties. */
    Map<String, ?> getKafkaProperties();
}
