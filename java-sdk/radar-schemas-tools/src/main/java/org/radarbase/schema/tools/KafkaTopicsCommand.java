package org.radarbase.schema.tools;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.registration.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a KafkaTopics command to register topics from the command line.
 */
public class KafkaTopicsCommand implements SubCommand {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicsCommand.class);

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

        try (KafkaTopics topics = new KafkaTopics(options.getString("bootstrapServers"))) {
            if (!topics.initialize(brokers)) {
                logger.error("Kafka brokers not yet available. Aborting.");
                return 1;
            }
            return topics.createTopics(app.getCatalogue(),
                    options.getInt("partitions"),
                    replication,
                    options.getString("topic"),
                    options.getString("match"));

        } catch (InterruptedException e) {
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
        parser.addArgument("bootstrapServers")
                .help("Kafka hosts, ports and protocols, comma-separated");
        SubCommand.addRootArgument(parser);
    }
}
