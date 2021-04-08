package org.radarbase.schema.registration;

import java.io.Closeable;
import java.util.Set;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;

import org.radarbase.schema.specification.SourceCatalogue;

/**
 * Registers topic on configured Kafka environment.
 */
public interface TopicRegistrar extends Closeable {

    /**
     * Create all topics in a catalogue based on pattern provided.
     *
     * @param catalogue   source catalogue to extract topic names from.
     * @param partitions  number of partitions per topic.
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
     * @param topics      names of the topic to create.
     * @param partitions  number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @return whether the topic was registered.
     */
    boolean createTopics(Stream<String> topics, int partitions, short replication);

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
}
