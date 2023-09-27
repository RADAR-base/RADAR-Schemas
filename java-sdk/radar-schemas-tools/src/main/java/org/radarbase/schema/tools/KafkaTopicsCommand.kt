package org.radarbase.schema.tools

import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.schema.registration.KafkaTopics
import org.radarbase.schema.registration.KafkaTopics.Companion.configureKafka
import org.radarbase.schema.specification.config.ToolConfig
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import org.slf4j.LoggerFactory

/**
 * Create a KafkaTopics command to register topics from the command line.
 */
class KafkaTopicsCommand : SubCommand {
    override val name = "create"

    override suspend fun execute(options: Namespace, app: CommandLineApp): Int {
        val brokers = options.getInt("brokers")
        val replication = options.getShort("replication") ?: 3
        if (brokers < replication) {
            logger.error(
                "Cannot assign a replication factor {}" +
                    " higher than number of brokers {}",
                replication,
                brokers,
            )
            return 1
        }
        val toolConfig: ToolConfig = app.config
            .configureKafka(bootstrapServers = options.getString("bootstrap_servers"))

        return KafkaTopics(toolConfig).use { topics ->
            try {
                val numTries = options.getInt("num_tries")
                topics.initialize(brokers, numTries)
            } catch (ex: IllegalStateException) {
                logger.error("Kafka brokers not yet available. Aborting.")
                return@use 1
            }
            topics.createTopics(
                app.catalogue,
                options.getInt("partitions") ?: 3,
                replication,
                options.getString("topic"),
                options.getString("match"),
            )
        }
    }

    override fun addParser(parser: ArgumentParser) {
        parser.apply {
            description("Create all topics that are missing on the Kafka server.")
            addArgument("-p", "--partitions")
                .help("number of partitions per topic")
                .type(Int::class.java).default = 3
            addArgument("-r", "--replication")
                .help("number of replicas per data packet")
                .type(Short::class.java).default = 3.toShort()
            addArgument("-b", "--brokers")
                .help("number of brokers that are expected to be available.")
                .type(Int::class.java).default = 3
            addArgument("-n", "--num-tries")
                .help("number of times to try the topic registration (in case there are failures).")
                .type(Int::class.java)
                .setDefault(20)
                .choices(SubCommand.IntRangeArgumentChoice(1, 100))
            addArgument("-t", "--topic")
                .help("register the schemas of one topic")
                .type(String::class.java)
            addArgument("-m", "--match")
                .help(
                    "register the schemas of all topics matching the given regex" +
                        "; does not do anything if --topic is specified",
                )
                .type(String::class.java)
            addArgument("-s", "--bootstrap-servers")
                .help("Kafka hosts, ports and protocols, comma-separated")
                .type(String::class.java)
            addRootArgument()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaTopicsCommand::class.java)
    }
}
