package org.radarbase.schema.registration;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Kafka topics with Zookeeper.
 */
public class KafkaTopics extends AbstractTopicRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTopics.class);
    private final String bootstrapServers;
    private AdminClient kafkaClient;
    private boolean initialized;

    /**
     * Create Kafka topics registration object with given Zookeeper.
     *
     * @param bootstrapServers comma-separated list of Kafka 'PROTOCOL://hostname:port'.
     */
    public KafkaTopics(@NotNull String bootstrapServers) {
        this.kafkaClient = AdminClient.create(Map.of(
                BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
        this.bootstrapServers = bootstrapServers;
        initialized = false;
    }

    /**
     * Wait for brokers to become available. This uses a polling mechanism, waiting for at most 200
     * seconds.
     *
     * @param brokers number of brokers to wait for
     * @return whether the brokers where available
     * @throws InterruptedException when waiting for the brokers is interrupted.
     */
    public boolean initialize(int brokers) throws InterruptedException {
        int sleep = 2;
        int numTries = 20;
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
                logger.error("Failed to connect to bootstrap server " + bootstrapServers,
                        ex.getCause());
                hosts = Collections.emptyList();
            }
            numBrokers = hosts.size();

            if (numBrokers >= brokers) {
                logger.info("Initialized Kafka client with bootstrap servers {}", hosts);
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

    public String getBootstrapServers() {
        ensureInitialized();
        return bootstrapServers;
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
}
