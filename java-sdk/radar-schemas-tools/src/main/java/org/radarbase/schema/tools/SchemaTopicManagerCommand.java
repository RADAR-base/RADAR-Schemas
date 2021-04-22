package org.radarbase.schema.tools;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import net.sourceforge.argparse4j.impl.action.StoreConstArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.registration.JsonSchemaBackupStorage;
import org.radarbase.schema.registration.KafkaTopics;
import org.radarbase.schema.registration.SchemaTopicManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaTopicManagerCommand implements SubCommand {

    private static final String SUBACTION = "subaction";
    private static final Logger logger = LoggerFactory.getLogger(SchemaTopicManagerCommand.class);

    @Override
    public String getName() {
        return "schema-topic";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        JsonSchemaBackupStorage jsonStorage = new JsonSchemaBackupStorage(
                Paths.get(options.getString("file")));

        Map<String, Object> kafkaConfig;
        try {
            kafkaConfig = KafkaTopics.loadConfig(
                    options.getString("kafka-config"),
                    options.getString("bootstrap-servers"));
        } catch (IOException | IllegalStateException ex) {
            logger.error("Cannot configure Kafka client: {}", ex.getMessage());
            return 1;
        }

        try (KafkaTopics topics = new KafkaTopics(kafkaConfig)) {
            SchemaTopicManager manager = new SchemaTopicManager(topics, jsonStorage);
            manager.initialize(options.getInt("brokers"));

            Duration timeout = Duration.ofSeconds(options.getInt("timeout"));

            switch (options.<SubAction>get(SUBACTION)) {
                case BACKUP:
                    manager.makeBackup(timeout);
                    break;
                case RESTORE:
                    manager.restoreBackup(options.getShort("replication"));
                    break;
                case ENSURE:
                    manager.ensure(options.getShort("replication"), timeout);
                    break;
                default:
                    logger.error("Unknown action");
                    return 3;
            }
            return 0;

        } catch (Exception ex) {
            logger.error("Action failed: {}", ex.toString());
            return 2;
        }
    }

    @Override
    public void addParser(ArgumentParser parser) {
        parser.description("Manage the _schemas topic");

        parser.addArgument("--backup")
                .help("back up schema topic data")
                .action(new StoreConstArgumentAction())
                .setConst(SubAction.BACKUP)
                .dest(SUBACTION);

        parser.addArgument("--restore")
                .help("restore schema topic from backup")
                .action(new StoreConstArgumentAction())
                .setConst(SubAction.RESTORE)
                .dest(SUBACTION);

        parser.addArgument("--ensure")
                .help("ensure that the schema topic is restored if needed")
                .action(new StoreConstArgumentAction())
                .setConst(SubAction.ENSURE)
                .dest(SUBACTION);

        parser.addArgument("-r", "--replication")
                .help("number of replicas per data packet")
                .type(Short.class)
                .setDefault((short) 3);

        parser.addArgument("-t", "--timeout")
                .help("time (seconds) to wait for records in the _schemas topic to become"
                        + " available")
                .setDefault(600)
                .type(Integer.class);

        parser.addArgument("-f", "--file")
                .help("JSON file to load _schemas from")
                .type(String.class)
                .required(true);

        parser.addArgument("-b", "--brokers")
                .help("number of brokers that are expected to be available.")
                .type(Integer.class)
                .setDefault(3);
        parser.addArgument("-s", "--bootstrap-servers")
                .help("Kafka hosts, ports and protocols, comma-separated")
                .type(String.class);
        parser.addArgument("-c", "--kafka-config")
                .help("File path for Kafka properties")
                .type(String.class);
        SubCommand.addRootArgument(parser);
    }

    private enum SubAction {
        BACKUP, RESTORE, ENSURE
    }
}
