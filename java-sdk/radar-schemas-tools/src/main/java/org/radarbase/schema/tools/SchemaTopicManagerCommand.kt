package org.radarbase.schema.tools

import net.sourceforge.argparse4j.impl.action.StoreConstArgumentAction
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.schema.registration.*
import org.radarbase.schema.registration.KafkaTopics.Companion.configureKafka
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.time.Duration

class SchemaTopicManagerCommand : SubCommand {
    override val name = "schema-topic"

    override fun execute(options: Namespace, app: CommandLineApp): Int {
        val toolConfig = app.config
                .configureKafka(bootstrapServers = options.getString("bootstrap_servers"))
        try {
            KafkaTopics(toolConfig).use { topics ->
                val jsonStorage = JsonSchemaBackupStorage(
                    Paths.get(options.getString("file")))
                val manager = SchemaTopicManager(topics, jsonStorage)
                manager.initialize(options.getInt("brokers"))
                val timeout = Duration.ofSeconds(options.getInt("timeout").toLong())
                when (options.get<SubAction>(SUBACTION)) {
                    SubAction.BACKUP -> manager.makeBackup(timeout)
                    SubAction.RESTORE -> manager.restoreBackup(options.getShort("replication"))
                    SubAction.ENSURE -> manager.ensure(options.getShort("replication"), timeout)
                    else -> {
                        logger.error("Unknown action")
                        return 3
                    }
                }
                return 0
            }
        } catch (ex: Exception) {
            logger.error("Action failed: {}", ex.toString())
            return 2
        }
    }

    override fun addParser(parser: ArgumentParser) {
        parser.apply {
            description("Manage the _schemas topic")
            addArgument("--backup")
                .help("back up schema topic data")
                .action(StoreConstArgumentAction())
                .setConst(SubAction.BACKUP)
                .dest(SUBACTION)
            addArgument("--restore")
                .help("restore schema topic from backup")
                .action(StoreConstArgumentAction())
                .setConst(SubAction.RESTORE)
                .dest(SUBACTION)
            addArgument("--ensure")
                .help("ensure that the schema topic is restored if needed")
                .action(StoreConstArgumentAction())
                .setConst(SubAction.ENSURE)
                .dest(SUBACTION)
            addArgument("-r", "--replication")
                .help("number of replicas per data packet")
                .type(Short::class.java).default = 3.toShort()
            addArgument("-t", "--timeout")
                .help("time (seconds) to wait for records in the _schemas topic to become"
                    + " available")
                .setDefault(600)
                .type(Int::class.java)
            addArgument("-f", "--file")
                .help("JSON file to load _schemas from")
                .type(String::class.java)
                .required(true)
            addArgument("-b", "--brokers")
                .help("number of brokers that are expected to be available.")
                .type(Int::class.java).default = 3
            addArgument("-s", "--bootstrap-servers")
                .help("Kafka hosts, ports and protocols, comma-separated")
                .type(String::class.java)
            addRootArgument()
        }
    }

    private enum class SubAction {
        BACKUP, RESTORE, ENSURE
    }

    companion object {
        private const val SUBACTION = "subaction"
        private val logger = LoggerFactory.getLogger(
            SchemaTopicManagerCommand::class.java)
    }
}
