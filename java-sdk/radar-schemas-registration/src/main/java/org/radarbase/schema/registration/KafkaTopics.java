package org.radarbase.schema.registration;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
import org.apache.kafka.common.Node;
import org.radarbase.schema.specification.SourceCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics implements TopicRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);
    private final Map<String, Object> kafkaProperties;
    private final AdminClient kafkaClient;
    private boolean initialized;
    static final int MAX_SLEEP = 32;
    private Set<String> topics;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     *
     * @param kafkaProperties Kafka properties that must include a bootstrap.servers
     *                        or zookeeper.connect property.
     */
    public KafkaTopics(@NotNull Map<String, Object> kafkaProperties) {
        initialized = false;
        this.kafkaProperties = kafkaProperties;
        this.kafkaClient = AdminClient.create(kafkaProperties);
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    @Override
    public void initialize(int brokers) throws InterruptedException {
        initialize(brokers, 20);
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
    @Override
    public void initialize(int brokers, int numTries) throws InterruptedException {
        int sleep = 2;
        int numBrokers = 0;

        for (int tries = 0; tries < numTries && numBrokers < brokers; tries++) {
            List<String> hosts;
            try {
                hosts = kafkaClient.describeCluster()
                        .nodes()
                        .get()
                        .stream()
                        .map(Node::host)
                        .collect(Collectors.toList());
            } catch (ExecutionException ex) {
                logger.error("Failed to connect to bootstrap server {}",
                        kafkaProperties.get(BOOTSTRAP_SERVERS_CONFIG), ex.getCause());
                hosts = Collections.emptyList();
            }
            numBrokers = hosts.size();

            if (numBrokers >= brokers) {
                break;
            } else if (tries < numTries - 1) {
                logger.warn("Only {} out of {} Kafka brokers available. Waiting {} seconds.",
                        numBrokers, brokers, sleep);
                Thread.sleep(sleep * 1000L);
                sleep = Math.min(MAX_SLEEP, sleep * 2);
            } else {
                logger.error("Only {} out of {} Kafka brokers available."
                        + " Failed to wait on all brokers.", numBrokers, brokers);
            }
        }

        initialized = numBrokers >= brokers;

        if (!initialized || !refreshTopics()) {
            throw new IllegalStateException("Brokers or topics not available.");
        }
    }

    @Override
    public void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Manager is not initialized yet");
        }
    }

    @Override
    public int createTopics(@NotNull SourceCatalogue catalogue, int partitions, short replication,
            String topic, String match) {
        Pattern pattern = TopicRegistrar.matchTopic(topic, match);

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
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    private boolean createTopics(@NotNull SourceCatalogue catalogue, int partitions,
            short replication) {
        ensureInitialized();
        return createTopics(catalogue.getTopicNames(), partitions, replication);
    }

    @Override
    public boolean createTopics(Stream<String> topicsToCreate, int partitions, short replication) {
        ensureInitialized();
        try {
            refreshTopics();
            logger.info("Creating topics. Topics marked with [*] already exist.");

            List<NewTopic> newTopics = topicsToCreate.sorted().distinct().filter(t -> {
                if (this.topics != null && this.topics.contains(t)) {
                    logger.info("[*] {}", t);
                    return false;
                } else {
                    logger.info("[ ] {}", t);
                    return true;
                }
            }).map(t -> new NewTopic(t, partitions, replication)).collect(Collectors.toList());

            if (!newTopics.isEmpty()) {
                getKafkaClient().createTopics(newTopics).all().get();
                logger.info("Created {} topics. Requesting to refresh topics", newTopics.size());
                refreshTopics();
            } else {
                logger.info("All of the topics are already created.");
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topics {}", ex.toString());
            return false;
        }
    }

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
                        + " Trying again after {} seconds.", e, sleep);
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
        kafkaClient.close();
    }

    /**
     * Get current number of Kafka brokers according to Zookeeper.
     *
     * @return number of Kafka brokers
     * @throws ExecutionException if kafka cannot connect
     * @throws InterruptedException if the query is interrupted.
     */
    public int getNumberOfBrokers() throws ExecutionException, InterruptedException {
        return kafkaClient.describeCluster()
                .nodes()
                .thenApply(Collection::size)
                .get();
    }

    @NotNull
    @Override
    public AdminClient getKafkaClient() {
        ensureInitialized();
        return kafkaClient;
    }

    @Override
    public Map<String, Object> getKafkaProperties() {
        return kafkaProperties;
    }

    public static Map<String, Object> loadConfig(
            final String configFile,
            String bootstrapServers
    ) throws IOException {
        Map<String, Object> kafkaConfig;
        if (configFile != null && !configFile.isEmpty()) {
            final Properties cfg = new Properties();
            try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
                cfg.load(inputStream);
            }
            kafkaConfig = cfg.entrySet().stream()
                    .collect(Collectors.toMap(e -> (String)e.getKey(), Entry::getValue));
        } else {
            kafkaConfig = new HashMap<>();
        }
        if (bootstrapServers != null && !bootstrapServers.isEmpty()) {
            kafkaConfig.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        }
        if (kafkaConfig.get(BOOTSTRAP_SERVERS_CONFIG) == null) {
            throw new IllegalStateException("Cannot configure Kafka without "
                    + BOOTSTRAP_SERVERS_CONFIG + " property");
        }
        return kafkaConfig;
    }
}
