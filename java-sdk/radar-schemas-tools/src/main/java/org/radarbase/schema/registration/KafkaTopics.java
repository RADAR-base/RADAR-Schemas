package org.radarbase.schema.registration;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.util.List;
import java.util.Map;
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
import org.apache.kafka.common.utils.Time;
import org.radarbase.schema.CommandLineApp;
import org.radarbase.schema.util.SubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConverters$;
import scala.collection.Seq;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics extends AbstractTopicRegistrar {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);

    private final KafkaZkClient zkClient;
    private AdminClient kafkaClient;
    private String bootstrapServers;
    private boolean initialized;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     *
     * @param zookeeper comma-separated list of Zookeeper 'hostname:port'.
     */
    public KafkaTopics(@NotNull String zookeeper) {
        this.zkClient = KafkaZkClient
                .apply(zookeeper, false, 15_000, 10_000, 30, Time.SYSTEM, "kafka.server",
                        "SessionExpireListener", Option.apply("radar-schemas"), Option.empty());
        bootstrapServers = null;
        initialized = false;
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism,
     * waiting for at most 200 seconds.
     *
     * @param brokers number of brokers to wait for
     * @return whether the brokers where available
     * @throws InterruptedException     when waiting for the brokers is interrupted.
     * @throws ZooKeeperClientException if Zookeeper cannot be initialized.
     */
    public boolean initialize(int brokers) throws InterruptedException {
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
        return initialized && refreshTopics();
    }

    @Override
    public void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Manager is not initialized yet");
        }
    }

    private List<Broker> currentBrokers() {
        try {
            // convert Scala sequence of servers to Java
            return asStream(zkClient.getAllBrokersInCluster()).collect(Collectors.toList());
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
     * Get current number of Kafka brokers according to Zookeeper.
     *
     * @return number of Kafka brokers
     * @throws ZooKeeperClientException if zookeeper cannot connect
     */
    public int getNumberOfBrokers() {
        return zkClient.getAllBrokersInCluster().length();
    }

    @NotNull
    public KafkaZkClient getZkClient() {
        return zkClient;
    }

    @NotNull
    @Override
    public AdminClient getKafkaClient() {
        ensureInitialized();
        return kafkaClient;
    }

    @Override
    public void close() {
        super.close();
        zkClient.close();
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
                return topics.createTopics(app.getCatalogue(), partitions, replication,
                        options.getString("topic"), options.getString("match"));

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
