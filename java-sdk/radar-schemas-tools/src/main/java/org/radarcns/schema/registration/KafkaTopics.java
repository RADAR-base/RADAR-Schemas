package org.radarcns.schema.registration;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer;
import kafka.utils.ZkUtils;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.KeeperException;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.util.SubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.radarcns.schema.CommandLineApp.matchTopic;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);
    private static final int MAX_SLEEP = 32;

    private final ZkUtils zkUtils;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     * @param zookeeper comma-separated list of Zookeeper 'hostname:port'.
     */
    public KafkaTopics(String zookeeper) {
        ZkClient zkClient = new ZkClient(zookeeper, 15_000, 10_000);

        zkClient.setZkSerializer(new ZkSerializer() {
            @Override
            public byte[] serialize(Object o) throws ZkMarshallingError {
                return ZKStringSerializer.serialize(o);
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return ZKStringSerializer.deserialize(bytes);
            }
        });

        zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeper), false);
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism,
     * waiting for at most 200 seconds.
     * @param brokers number of brokers to wait for
     * @return whether the brokers where available
     * @throws InterruptedException
     * @throws KeeperException
     */
    public boolean waitForBrokers(int brokers) throws InterruptedException, KeeperException {
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
        return brokersAvailable;
    }

    /**
     * Create all topics in a catalogue.
     * @param catalogue source catalogue to extract topic names from
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the whole catalogue was registered
     */
    public boolean createTopics(SourceCatalogue catalogue, int partitions, int replication) {
        return catalogue.getTopicNames()
                .allMatch(topic -> createTopic(topic, partitions, replication));
    }

    /**
     * Create a single topic.
     * @param topic name of the topic to create
     * @param partitions number of partitions per topic
     * @param replication number of replicas for a topic
     * @return whether the topic was registered
     */
    public boolean createTopic(String topic, int partitions, int replication) {
        Properties props = new Properties();
        try {
            if (!AdminUtils.topicExists(zkUtils, topic)) {
                logger.info("Creating topic {}", topic);
                AdminUtils.createTopic(zkUtils, topic, partitions, replication, props,
                        RackAwareMode.Enforced$.MODULE$);
            } else {
                logger.info("Topic {} already exists", topic);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to create topic {}", topic, ex);
            return false;
        }
    }

    public int getNumberOfBrokers() throws KeeperException, InterruptedException {
        return zkUtils.getAllBrokersInCluster().length();
    }

    @Override
    public void close() {
        zkUtils.close();
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
            int replication = options.getInt("replication");

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
                    Optional<Boolean> result = app.getCatalogue().getTopicNames()
                            .filter(s -> pattern.matcher(s).find())
                            .map(s -> topics.createTopic(s, partitions, replication))
                            .reduce((a, b) -> a && b);
                    if (!result.isPresent()) {
                        logger.error("Topic {} does not match a known topic."
                                        + " Find the list of acceptable topics"
                                        + " with the `radar-schemas-tools list` command. Aborting.",
                                pattern);
                        return 1;
                    } else {
                        return result.get() ? 0 : 1;
                    }
                }
            } catch (InterruptedException | KeeperException e) {
                logger.error("Cannot retrieve number of active Kafka brokers."
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
                    .type(Integer.class)
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
