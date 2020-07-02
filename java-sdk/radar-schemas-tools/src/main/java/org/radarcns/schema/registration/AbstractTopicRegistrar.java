package org.radarcns.schema.registration;

import static org.radarcns.schema.CommandLineApp.matchTopic;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.radarcns.schema.specification.SourceCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTopicRegistrar implements TopicRegistrar {
    static final int MAX_SLEEP = 32;
    private static final Logger logger = LoggerFactory.getLogger(AbstractTopicRegistrar.class);
    private Set<String> topics;

    /**
     * Create all topics in a catalogue based on pattern provided.
     *
     * @param catalogue   source catalogue to extract topic names from.
     * @param partitions  number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @return 0 if execution was successful. 1 otherwise.
     */
    @Override
    public int createTopics(@NotNull SourceCatalogue catalogue, int partitions, short replication,
            String topic, String match) {
        Pattern pattern = matchTopic(topic, match);

        if (pattern == null) {
            return createTopics(catalogue, partitions, replication) ? 0 : 1;
        } else {
            List<String> topicNames =
                    catalogue.getTopicNames().filter(s -> pattern.matcher(s).find())
                            .collect(Collectors.toList());

            if (topicNames.isEmpty()) {
                logger.error("Topic {} does not match a known topic."
                        + " Find the list of acceptable topics"
                        + " with the `radar-schemas-tools list` command. Aborting.", pattern);
                return 1;
            }
            return createTopics(topicNames.stream(), partitions, replication) ? 0 : 1;
        }
    }


    /**
     * Create all topics in a catalogue.
     *
     * @param catalogue   source catalogue to extract topic names from
     * @param partitions  number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    private boolean createTopics(@NotNull SourceCatalogue catalogue, int partitions,
            short replication) {
        ensureInitialized();
        return createTopics(catalogue.getTopicNames(), partitions, replication);
    }

    /**
     * Create a single topic.
     *
     * @param topics      names of the topic to create.
     * @param partitions  number of partitions per topic.
     * @param replication number of replicas for a topic.
     * @return whether the topic was registered.
     */
    @Override
    public boolean createTopics(Stream<String> topics, int partitions, short replication) {
        ensureInitialized();
        try {
            logger.info("Creating topics. Topics marked with [*] already exist.");

            List<NewTopic> newTopics = topics.sorted().distinct().filter(t -> {
                if (this.topics.contains(t)) {
                    logger.info("[*] {}", t);
                    return false;
                } else {
                    logger.info("[ ] {}", t);
                    return true;
                }
            }).map(t -> new NewTopic(t, partitions, replication)).collect(Collectors.toList());

            if (!newTopics.isEmpty()) {
                getKafkaClient().createTopics(newTopics).all().get();
                refreshTopics();
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topics {}", ex.toString());
            return false;
        }
    }

    /**
     * Refresh the list of topics from Kafka.
     *
     * @return {@code true} if the update succeeded, {@code false} otherwise.
     * @throws InterruptedException if the request was interrupted.
     */
    @Override
    public boolean refreshTopics() throws InterruptedException {
        ensureInitialized();
        logger.info("Waiting for topics to become available.");
        int sleep = 10;
        int numTries = 10;

        topics = null;
        ListTopicsOptions opts = new ListTopicsOptions().listInternal(true);
        for (int tries = 0; tries < numTries; tries++) {
            try {
                topics = getKafkaClient().listTopics(opts).names().get(sleep, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                logger.error("Failed to list topics from brokers: {}."
                        + " Trying again after {} seconds.", e.toString(), sleep);
                Thread.sleep(sleep * 1000L);
                sleep = Math.min(MAX_SLEEP, sleep * 2);
                continue;
            } catch (TimeoutException e) {
                // do nothing
            }
            if (topics != null && !topics.isEmpty()) {
                break;
            }
            if (tries < numTries - 1) {
                logger.warn("Topics not listed yet after {} seconds", sleep);
            } else {
                logger.error("Topics have not become available. Failed to wait on Kafka.");
            }
            sleep = Math.min(MAX_SLEEP, sleep * 2);
        }

        if (topics == null || topics.isEmpty()) {
            return false;
        } else {
            Thread.sleep(5000L);
            return true;
        }
    }

    @Override
    public Set<String> getTopics() {
        ensureInitialized();
        return Collections.unmodifiableSet(topics);
    }


    @Override
    public void close() {
        if (getKafkaClient() != null) {
            getKafkaClient().close();
        }
    }

    abstract AdminClient getKafkaClient();

}
