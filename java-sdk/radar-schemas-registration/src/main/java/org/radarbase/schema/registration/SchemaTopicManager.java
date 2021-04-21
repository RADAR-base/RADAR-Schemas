package org.radarbase.schema.registration;

import static org.apache.kafka.common.config.ConfigResource.Type.TOPIC;

import io.confluent.kafka.schemaregistry.storage.serialization.SchemaRegistrySerializer;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.AlterConfigOp.OpType;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the _schemas topic. Currently, this backs up and restores all data in the _schemas
 * topic.
 */
@SuppressWarnings("WeakerAccess")
public class SchemaTopicManager {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTopicManager.class);
    private static final String TOPIC_NAME = "_schemas";
    private static final Duration SECONDARY_TIMEOUT = Duration.ofSeconds(10L);
    private final KafkaTopics topics;
    private final SchemaBackupStorage storage;
    private final SchemaRegistrySerializer serializer;
    private final ConfigResource topicResource;
    private boolean isInitialized;

    /**
     * Schema topic manager.
     *
     * @param zookeeper zookeeper hosts and ports, comma-separated
     * @param storage storage medium to read and write backups from and to.
     */
    public SchemaTopicManager(@NotNull String zookeeper, @NotNull SchemaBackupStorage storage) {
        topics = new KafkaTopics(zookeeper);
        this.storage = storage;
        serializer = new SchemaRegistrySerializer();
        topicResource = new ConfigResource(TOPIC, TOPIC_NAME);
        isInitialized = false;
    }

    /**
     * Wait for brokers and topics to become available.
     *
     * @param numBrokers number of brokers to wait for
     * @throws InterruptedException if waiting for the brokers or topics was interrupted.
     * @throws IllegalStateException if the brokers or topics are not available
     */
    public void initialize(int numBrokers) throws InterruptedException {
        if (!topics.initialize(numBrokers)) {
            throw new IllegalStateException("Brokers or topics not available.");
        }
        isInitialized = true;
    }

    private void ensureInitialized() {
        if (!isInitialized) {
            throw new IllegalStateException("Manager is not initialized yet");
        }
    }

    /**
     * Read a backup from the _schemas topic. This backup only includes the actual schemas, not
     * configuration changes or NOOP messages.
     *
     * @param timeout time to wait for first schema records to become available.
     * @return backup of the _schemas topic.
     * @throws IOException if a message in the topic cannot be read
     * @throws ExecutionException if the topic configuration cannot be read
     * @throws InterruptedException if the process was interrupted before finishing
     * @throws RuntimeException storage failure or any other error.
     * @throws IllegalStateException if this manager was not initialized
     */
    @NotNull
    public SchemaTopicBackup readBackup(Duration timeout)
            throws IOException, ExecutionException, InterruptedException {
        ensureInitialized();

        SchemaTopicBackup storeTopic = new SchemaTopicBackup();

        try {
            readSchemas(getConsumerProps(), storeTopic, timeout);

            storeTopic.setConfig(topics.getKafkaClient()
                    .describeConfigs(List.of(topicResource))
                    .values()
                    .get(topicResource)
                    .get());
        } catch (IOException e) {
            logger.error("Failed to deserialize the schema or config key", e);
            throw e;
        } catch (ExecutionException e) {
            logger.error("Failed to get _schemas config", e);
            throw e;
        } catch (RuntimeException ex) {
            logger.error("Failed to store schemas", ex);
            throw ex;
        } catch (InterruptedException ex) {
            logger.error("Failed waiting for _schemas records", ex);
            Thread.currentThread().interrupt();
            throw ex;
        }

        return storeTopic;
    }

    /**
     * Read a backup from the _schemas topic and store it. This backup only includes the actual
     * schemas, not configuration changes or NOOP messages.
     *
     * @param timeout time to wait for first schema records to become available.
     * @throws IOException if a message in the topic cannot be read
     * @throws ExecutionException if the topic configuration cannot be read
     * @throws InterruptedException if the process was interrupted before finishing
     * @throws RuntimeException storage failure or any other error.
     * @throws IllegalStateException if this manager was not initialized
     */
    public void makeBackup(Duration timeout)
            throws IOException, InterruptedException, ExecutionException {
        SchemaTopicBackup storeTopic = readBackup(timeout);
        if (storeTopic != null) {
            try {
                if (storeTopic.startsAtFirstId()) {
                    storage.store(storeTopic);
                } else {
                    storage.storeInvalid(storeTopic);
                }
            } catch (IOException e) {
                logger.error("Failed to store _schemas data", e);
                throw e;
            }
        }
    }

    @NotNull
    private Map<String, Object> getConsumerProps() {
        Map<String, Object> consumerProps = new HashMap<>();

        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG,
                "schema-backup-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "schema-backup");

        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, topics.getBootstrapServers());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.ByteArrayDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.ByteArrayDeserializer.class);
        return consumerProps;
    }

    @NotNull
    private Map<String, Object> getProducerProps() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, "schema-backup");

        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, topics.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.ByteArraySerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.ByteArraySerializer.class);
        return producerProps;
    }

    /**
     * Checks if the current _schemas topic is valid. If not, the topic is replaced by the data in
     * the backup. The _schemas topic is considered valid if it exists, is not empty and starts at
     * id 1.
     *
     * @param timeout time to wait for first schema records to become available.
     * @throws IOException if a message in the topic cannot be read or written
     * @throws ExecutionException if the topic configuration cannot be read or written
     * @throws InterruptedException if the process was interrupted before finishing
     * @throws RuntimeException storage failure or any other error.
     * @throws IllegalStateException if this manager was not initialized or schema registry was
     *                               running
     */
    public void ensure(short replication, Duration timeout)
            throws InterruptedException, ExecutionException, IOException {
        ensureInitialized();

        boolean topicExists = topics.getTopics().contains(TOPIC_NAME);
        if (topicExists) {
            SchemaTopicBackup backup = readBackup(timeout);
            if (backup.startsAtFirstId()) {
                logger.info("Existing topic is valid.");
                return;
            }

            try {
                storage.storeInvalid(backup);
            } catch (IOException e) {
                logger.error("Backup storage failure.", e);
                throw e;
            }
        }
        SchemaTopicBackup newBackup;
        try {
            newBackup = storage.load();
        } catch (IOException e) {
            logger.error("Backup storage loading failure.", e);
            throw e;
        }
        if (newBackup == null) {
            logger.error("No valid backup in storage.");
            return;
        }

        // Backups successful. Remove old topic.
        if (topicExists) {
            topics.getKafkaClient().deleteTopics(List.of(TOPIC_NAME))
                    .all().get();
            try {
                topics.refreshTopics();
            } catch (InterruptedException e) {
                logger.info("Failed to wait to refresh topics");
                Thread.currentThread().interrupt();
                throw e;
            }
        }

        if (!topics.createTopics(Stream.of(TOPIC_NAME), 1, replication)) {
            throw new IllegalStateException("Failed to create _schemas topic");
        }

        commitBackup(newBackup);
    }

    /**
     * Read the schemas in the _schemas topic and put them in a backup.
     */
    private void readSchemas(Map<String, Object> consumerProps, SchemaTopicBackup storeTopic,
            Duration timeout) throws IOException {
        try (Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(consumerProps)) {
            ensurePartitions(consumer);

            TopicPartition topicPartition = new TopicPartition(TOPIC_NAME, 0);
            consumer.assign(List.of(topicPartition));
            consumer.seekToBeginning(List.of(topicPartition));

            logger.debug("Kafka store reader thread started");

            int numRecords = -1;
            Duration duration = timeout;

            while (numRecords != 0) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(duration);
                duration = SECONDARY_TIMEOUT;
                numRecords = records.count();
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    storeTopic.addSchemaRecord(serializer, record);
                }
            }
        }
    }

    /**
     * Ensure that _schemas has exactly one partition.
     *
     * @param consumer consumer to subscribe partitions with.
     * @throws IllegalArgumentException if the _schemas topic cannot be found.
     * @throws IllegalStateException if the _schemas topic has more than one partition.
     */
    private void ensurePartitions(Consumer<?, ?> consumer) {
        // Include a few retries since topic creation may take some time to propagate and schema
        // registry is often started immediately after creating the schemas topic.
        int retries = 0;
        List<PartitionInfo> partitions;
        do {
            partitions = consumer.partitionsFor(TOPIC_NAME);
            if (partitions != null && !partitions.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            retries++;
        }
        while (retries < 10);

        if (partitions == null || partitions.isEmpty()) {
            throw new IllegalArgumentException("Unable to subscribe to the Kafka topic "
                    + TOPIC_NAME
                    + " backing this data store. Topic may not exist.");
        } else if (partitions.size() > 1) {
            throw new IllegalStateException("Unexpected number of partitions in the "
                    + TOPIC_NAME
                    + " topic. Expected 1 and instead got " + partitions.size());
        }
    }

    /**
     * Restores the _schemas from backup.
     *
     * @throws RuntimeException storage failure or any other error.
     * @throws ExecutionException if the topic configuration cannot be written
     * @throws IllegalStateException if this manager was not initialized or if the schema registry
     *                               is running.
     */
    public void restoreBackup(short replication)
            throws IOException, ExecutionException, InterruptedException {
        ensureInitialized();
        SchemaTopicBackup storeTopic;
        try {
            storeTopic = storage.load();
        } catch (IOException e) {
            logger.error("Failed to load _schemas data", e);
            throw e;
        }

        if (storeTopic == null) {
            logger.error("Backup not available");
            return;
        }

        if (topics.getTopics().contains(TOPIC_NAME)) {
            throw new IllegalStateException(
                    "Topic _schemas already exists, cannot restore it from backup");
        }

        if (!topics.createTopics(Stream.of(TOPIC_NAME), 1, replication)) {
            throw new IllegalStateException("Failed to create _schemas topic");
        }

        commitBackup(storeTopic);
    }

    private void commitBackup(SchemaTopicBackup backup)
            throws ExecutionException, InterruptedException {
        AlterConfigsResult alterResult = topics.getKafkaClient()
                .incrementalAlterConfigs(Map.of(topicResource, backup.getConfig().entries().stream()
                        .map(e -> new AlterConfigOp(e, OpType.SET))
                        .collect(Collectors.toList())));

        try (KafkaProducer<byte[], byte[]> producer = new KafkaProducer<>(getProducerProps())) {
            List<Future<RecordMetadata>> futures = backup.getRecords().stream()
                    .map(r -> new ProducerRecord<>(TOPIC_NAME, r.getKey(), r.getValue()))
                    .map(producer::send)
                    .collect(Collectors.toList());

            // collect so we can do a blocking operation after all records have been sent
            for (Future<?> future : futures) {
                future.get();
            }
        }

        alterResult.all().get();
    }
}
