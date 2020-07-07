package org.radarcns.schema.registration;

import static org.radarbase.util.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.validation.constraints.NotNull;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.kafka.clients.admin.AdminClient;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.util.SubCommand;
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
        ensureInitialized();
        return kafkaClient;
    }

    @Override
    public void ensureInitialized() {
        if (this.kafkaClient != null) {
            throw new IllegalStateException("Kafka client is not initialized yet");
        }
    }

    private Properties loadConfig(final String configFile) throws IOException {
        Path filePath = Paths.get(configFile);
        if (!Files.exists(filePath)) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            cfg.load(inputStream);
        }
        return cfg;
    }


    /**
     * Create a ConfluentCloudTopics command to register topics from the command line.
     */
    public static SubCommand command() {
        return new ConfluentCloudTopicsCommand();
    }


    private static class ConfluentCloudTopicsCommand implements SubCommand {
        @Override
        public String getName() {
            return "cc-topic-create";
        }

        @Override
        public int execute(Namespace options, CommandLineApp app) {
            String configPath = options.getString("config-path");
            if (isNullOrEmpty(configPath)) {
                throw new IllegalArgumentException("--config-path not found. Confluent "
                        + "cloud config path cannot be empty");
            }
            short replication = options.getShort("replication");
            int partitions = options.getInt("partitions");

            try (TopicRegistrar topics = new ConfluentCloudTopics(configPath)) {
                return topics.createTopics(app.getCatalogue(), partitions, replication,
                        options.getString("topic"), options.getString("match"));

            } catch (IOException e) {
                logger.error("Could not load config file", e);
                return 1;
            }
        }

        @Override
        public void addParser(ArgumentParser parser) {
            parser.description("Create all topics that are missing on the Confluent Cloud env.");
            parser.addArgument("-c", "--config-path").help("File path for Confluent cloud config")
                .type(String.class);
            parser.addArgument("-p", "--partitions").help("number of partitions per topic")
                .type(Integer.class).setDefault(3);
            parser.addArgument("-r", "--replication").help("number of replicas per data packet")
                .type(Short.class).setDefault((short) 3);
            parser.addArgument("-t", "--topic").help("register the schemas of one topic")
                .type(String.class);
            parser.addArgument("-m", "--match").help(
                "register the schemas of all topics matching the given regex"
                    + "; does not do anything if --topic is specified").type(String.class);

            SubCommand.addRootArgument(parser);
        }
    }


}
