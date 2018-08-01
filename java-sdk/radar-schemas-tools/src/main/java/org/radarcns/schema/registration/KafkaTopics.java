package org.radarcns.schema.registration;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.radarcns.schema.CommandLineApp.matchTopic;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kafka.zk.KafkaZkClient;
import kafka.zookeeper.ZooKeeperClientException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.utils.Time;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.util.SubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters$;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);
    private static final int MAX_SLEEP = 32;

    private final KafkaZkClient zkClient;
    private AdminClient kafkaClient;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     * @param zookeeper comma-separated list of Zookeeper 'hostname:port'.
     */
    public KafkaTopics(String zookeeper) {
        this.zkClient = KafkaZkClient
                .apply(zookeeper, false, 15_000, 10_000, 30, Time.SYSTEM, "kafka.server",
                        "SessionExpireListener");
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism,
     * waiting for at most 200 seconds.
     * @param brokers number of brokers to wait for
     * @return whether the brokers where available
     * @throws InterruptedException when waiting for the brokers is interrepted.
     */
    public boolean waitForBrokers(int brokers) throws InterruptedException,
            ZooKeeperClientException {
        boolean brokersAvailable = false;
        int sleep = 2;
        for (int tries = 0; tries < 10; tries++) {
            int activeBrokers = getNumberOfBrokers();
            brokersAvailable = activeBrokers >= brokers;
            if (brokersAvailable) {
                logger.info("Kafka brokers available. Starting topic creation.");
                break;
            }

            if (tries < 9) {
                logger.warn("Only {} out of {} Kafka brokers available. Waiting {} seconds.",
                        activeBrokers, brokers, sleep);
                Thread.sleep(sleep * 1000L);
                sleep = Math.min(MAX_SLEEP, sleep * 2);
            } else {
                logger.error("Only {} out of {} Kafka brokers available."
                                + " Failed to wait on all brokers.",
                        activeBrokers, brokers, sleep);
            }
        }

        String bootstrapServers = JavaConverters$.MODULE$
                .seqAsJavaList(zkClient.getAllBrokersInCluster()).stream()
                .map(b -> b.endPoints().mkString(","))
                .collect(Collectors.joining(","));

        kafkaClient = AdminClient.create(Collections.singletonMap(
                BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));

        return brokersAvailable;
    }

    /**
     * Create all topics in a catalogue.
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    public boolean createTopics(SourceCatalogue catalogue, int partitions, short replication) {
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
        try {
            Set<String> existingTopics = kafkaClient.listTopics().names().get();

            List<NewTopic> newTopics = topics
                    .filter(t -> !existingTopics.contains(t))
                    .map(t -> new NewTopic(t, partitions, replication))
                    .collect(Collectors.toList());

            kafkaClient.createTopics(newTopics).all().get();
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topics {}", ex.toString());
            return false;
        }
    }

    public int getNumberOfBrokers() throws ZooKeeperClientException {
        return zkClient.getAllBrokersInCluster().length();
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
                if (!topics.waitForBrokers(brokers)) {
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
                    .setDefault(3);
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
