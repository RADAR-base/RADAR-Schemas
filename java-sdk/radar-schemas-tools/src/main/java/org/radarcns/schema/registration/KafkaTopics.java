package org.radarcns.schema.registration;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.radarcns.schema.CommandLineApp.matchTopic;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import kafka.cluster.Broker;
import kafka.cluster.EndPoint;
import kafka.zk.KafkaZkClient;
import kafka.zookeeper.ZooKeeperClientException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.utils.Time;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.util.SubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConverters$;
import scala.collection.Seq;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);
    private static final int MAX_SLEEP = 32;

    private final KafkaZkClient zkClient;
    private AdminClient kafkaClient;
    private String bootstrapServers;
    private Set<String> topics;
    private boolean initialized;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     * @param zookeeper comma-separated list of Zookeeper 'hostname:port'.
     */
    public KafkaTopics(@NotNull String zookeeper) {
        this.zkClient = KafkaZkClient
                .apply(zookeeper, false, 15_000, 10_000, 30, Time.SYSTEM, "kafka.server",
                        "SessionExpireListener", Option.apply("radar-schemas"));
        bootstrapServers = null;
        initialized = false;
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism,
     * waiting for at most 200 seconds.
     * @param brokers number of brokers to wait for
     * @return whether the brokers where available
     * @throws InterruptedException when waiting for the brokers is interrepted.
     */
    public boolean initialize(int brokers) throws InterruptedException,
            ZooKeeperClientException {
        int sleep = 2;
        int numTries = 20;
        int numBrokers = 0;

        for (int tries = 0; tries < numTries && numBrokers < brokers; tries++) {
            List<Broker> brokerList = currentBrokers();
            numBrokers = brokerList.size();

            if (numBrokers >= brokers) {
                // wait for 5sec before proceeding with topic creation
                bootstrapServers = brokerList.stream()
                        .map(Broker::endPoints)
                        .flatMap(KafkaTopics::asStream)
                        .map(EndPoint::connectionString)
                        .collect(Collectors.joining(","));

                logger.info("Creating Kafka client with bootstrap servers {}", bootstrapServers);
                kafkaClient = AdminClient.create(Map.of(
                        BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
            } else if (tries < numTries - 1) {
                logger.warn(
                        "Only {} out of {} Kafka brokers available. Waiting {} seconds.",
                        numBrokers, brokers, sleep);
                Thread.sleep(sleep * 1000L);
                sleep = Math.min(MAX_SLEEP, sleep * 2);
            } else {
                logger.error("Only {} out of {} Kafka brokers available."
                                + " Failed to wait on all brokers.",
                        numBrokers, brokers);
            }
        }

        initialized = numBrokers >= brokers;
        return initialized && refreshTopics();
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Manager is not initialized yet");
        }
    }

    /**
     * Refresh the list of topics from Kafka
     * @return {@code true} if the update succeeded, {@code false} otherwise.
     * @throws InterruptedException if the request was interrupted.
     */
    public boolean refreshTopics() throws InterruptedException {
        ensureInitialized();
        logger.info("Waiting for topics to become available.");
        int sleep = 10;
        int numTries = 10;

        topics = null;
        ListTopicsOptions opts = new ListTopicsOptions().listInternal(true);
        for (int tries = 0; tries < numTries; tries++) {
            try {
                topics = kafkaClient.listTopics(opts).names()
                        .get(sleep, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                logger.error("Failed to list topics from brokers: {}."
                                + " Trying again after {} seconds.",
                        e.toString(), sleep);
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

    public Set<String> getTopics() {
        ensureInitialized();
        return Collections.unmodifiableSet(topics);
    }

    private List<Broker> currentBrokers() {
        try {
            // convert Scala sequence of servers to Java
            return asStream(zkClient.getAllBrokersInCluster())
                    .collect(Collectors.toList());
        } catch (ZooKeeperClientException ex) {
            logger.warn("Failed to reach zookeeper");
            return List.of();
        }
    }

    private static <T> Stream<T> asStream(Seq<T> stream) {
        return JavaConverters$.MODULE$.seqAsJavaList(stream).stream();
    }

    public String getBootstrapServers() {
        ensureInitialized();
        return bootstrapServers;
    }

    /**
     * Create all topics in a catalogue.
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    public boolean createTopics(@NotNull SourceCatalogue catalogue, int partitions,
            short replication) {
        ensureInitialized();
        return createTopics(catalogue.getTopicNames(), partitions, replication);
    }

    /**
     * Create a single topic.
     * @param topics names of the topic to create
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the topic was registered
     */
    public boolean createTopics(Stream<String> topics, int partitions, short replication) {
        ensureInitialized();
        try {
            logger.info("Creating topics. Topics marked with [*] already exist.");

            List<NewTopic> newTopics = topics
                    .sorted()
                    .distinct()
                    .filter(t -> {
                        if (this.topics.contains(t)) {
                            logger.info("[*] {}", t);
                            return false;
                        } else {
                            logger.info("[ ] {}", t);
                            return true;
                        }
                    })
                    .map(t -> new NewTopic(t, partitions, replication))
                    .collect(Collectors.toList());

            if (!newTopics.isEmpty()) {
                kafkaClient.createTopics(newTopics).all().get();
                refreshTopics();
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topics {}", ex.toString());
            return false;
        }
    }

    public int getNumberOfBrokers() throws ZooKeeperClientException {
        return zkClient.getAllBrokersInCluster().length();
    }

    @NotNull
    public KafkaZkClient getZkClient() {
        return zkClient;
    }

    @NotNull
    public AdminClient getKafkaClient() {
        ensureInitialized();
        return kafkaClient;
    }

    @Override
    public void close() {
        zkClient.close();
        if (kafkaClient != null) {
            kafkaClient.close();
        }
    }

    /**
     * Create a KafkaTopics command to register topics from the command line.
     */
    public static SubCommand command() {
        return new KafkaTopicsCommand();
    }

    private static class KafkaTopicsCommand implements SubCommand {
        @Override
        public String getName() {
            return "create";
        }

        @Override
        public int execute(Namespace options, CommandLineApp app) {
            int brokers = options.getInt("brokers");
            short replication = options.getShort("replication");

            if (brokers < replication) {
                logger.error("Cannot assign a replication factor {}"
                                + " higher than number of brokers {}", replication, brokers);
                return 1;
            }

            int partitions = options.getInt("partitions");
            String zookeeper = options.getString("zookeeper");
            try (KafkaTopics topics = new KafkaTopics(zookeeper)) {
                if (!topics.initialize(brokers)) {
                    logger.error("Kafka brokers not yet available. Aborting.");
                    return 1;
                }

                Pattern pattern = matchTopic(
                        options.getString("topic"), options.getString("match"));

                if (pattern == null) {
                    return topics.createTopics(app.getCatalogue(), partitions, replication) ? 0 : 1;
                } else {
                    List<String> topicNames = app.getCatalogue().getTopicNames()
                            .filter(s -> pattern.matcher(s).find())
                            .collect(Collectors.toList());

                    if (topicNames.isEmpty()) {
                        logger.error("Topic {} does not match a known topic."
                                        + " Find the list of acceptable topics"
                                        + " with the `radar-schemas-tools list` command. Aborting.",
                                pattern);
                        return 1;
                    }

                    return topics.createTopics(topicNames.stream(), partitions, replication)
                            ? 0 : 1;
                }
            } catch (InterruptedException | ZooKeeperClientException e) {
                logger.error("Cannot retrieve number of addActive Kafka brokers."
                        + " Please check that Zookeeper is running.");
                return 1;
            }
        }

        @Override
        public void addParser(ArgumentParser parser) {
            parser.description("Create all topics that are missing on the Kafka server.");
            parser.addArgument("-p", "--partitions")
                    .help("number of partitions per topic")
                    .type(Integer.class)
                    .setDefault(3);
            parser.addArgument("-r", "--replication")
                    .help("number of replicas per data packet")
                    .type(Short.class)
                    .setDefault((short) 3);
            parser.addArgument("-b", "--brokers")
                    .help("number of brokers that are expected to be available.")
                    .type(Integer.class)
                    .setDefault(3);
            parser.addArgument("-t", "--topic")
                    .help("register the schemas of one topic")
                    .type(String.class);
            parser.addArgument("-m", "--match")
                    .help("register the schemas of all topics matching the given regex"
                            + "; does not do anything if --topic is specified")
                    .type(String.class);
            parser.addArgument("zookeeper")
                    .help("zookeeper hosts and ports, comma-separated");
            SubCommand.addRootArgument(parser);
        }
    }
}
