package org.radarbase.schema.registration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.validation.constraints.NotNull;
import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfluentCloudTopics extends AbstractTopicRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(ConfluentCloudTopics.class);

    private final AdminClient kafkaClient;

    /**
     * Create Kafka topics registration object with given Confluent Cloud bootstrap server
     * configurations.
     *
     * @param configPath File path of the Confluent-cloud config .
     */
    public ConfluentCloudTopics(@NotNull String configPath) throws IOException {

        logger.info("Creating Kafka client with bootstrap servers {}", configPath);

        this.kafkaClient = AdminClient.create(loadConfig(configPath));
    }

    @Override
    AdminClient getKafkaClient() {
        return kafkaClient;
    }

    @Override
    public void ensureInitialized() {
        // instance is already initialized at object creation.
    }

    private Properties loadConfig(final String configFile) throws IOException {
        final Properties cfg = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(configFile))) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
