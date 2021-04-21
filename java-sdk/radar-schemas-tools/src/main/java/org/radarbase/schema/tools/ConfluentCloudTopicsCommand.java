package org.radarbase.schema.tools;


import java.io.IOException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.registration.ConfluentCloudTopics;
import org.radarbase.schema.registration.TopicRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfluentCloudTopicsCommand implements SubCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfluentCloudTopicsCommand.class);

    @Override
    public String getName() {
        return "cc-topic-create";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        String configPath = options.getString("config");
        logger.debug("Config path is {}", configPath);
        if (configPath == null || configPath.isBlank()) {
            throw new IllegalArgumentException("--config not found. Confluent "
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
        parser.addArgument("-c", "--config").help("File path for Confluent cloud config")
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
