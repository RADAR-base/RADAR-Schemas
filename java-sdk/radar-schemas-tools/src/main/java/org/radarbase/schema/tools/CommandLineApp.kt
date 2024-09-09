/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.schema.tools

import kotlinx.coroutines.runBlocking
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.DataTopic
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.ToolConfig
import org.radarbase.schema.specification.config.loadToolConfig
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.system.exitProcess

/**
 * Command line app containing a source catalogue.
 * It starts at a RADAR-Schemas root. The source catalogue is read from the
 * `specifications` directory in that root.
 * @param root path to the root of a RADAR-Schemas directory.
 * @throws IOException if the source catalogue cannot be loaded.
 */
class CommandLineApp(
    val root: Path,
    val config: ToolConfig,
    val catalogue: SourceCatalogue,
) {
    init {
        logger.info("radar-schema-tools is initialized with root directory {}", this.root)
    }

    val topicsToCreate: Stream<String>
        get() = Stream.concat(
            catalogue.topicNames,
            config.topics.keys.stream(),
        )

    val rawTopics: Stream<String>
        get() = Stream.of(
            catalogue.passiveSources,
            catalogue.activeSources,
            catalogue.monitorSources,
            catalogue.connectorSources,
            catalogue.pushSources,
        )
            .flatMap { it.stream() }
            .flatMap { it.topicNames }

    val resultsCacheTopics: Stream<String>
        get() = catalogue.streamGroups.stream()
            .flatMap { it.timedTopicNames }

    fun getTopicsVerbose(prettyPrint: Boolean, source: String?): Stream<String> {
        var stream = catalogue.sources.parallelStream()
        if (source != null) {
            stream = stream.filter { s ->
                source.equals(s.verboseName, ignoreCase = true)
            }
        }
        return stream.map { s ->
            val dataTopicsString = s.data.asSequence()
                .sortedBy { it.topic }
                .joinToString(separator = "\n") { it.toVerboseString(prettyPrint) }
            "${s.verboseName}\n$dataTopicsString"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CommandLineApp::class.java)

        fun DataTopic.toVerboseString(prettyPrint: Boolean): String {
            val details = toString(prettyPrint)
                .dropLast(1)
                .replace("\n", "\n    ")
            return "  ${topic}\n    $details"
        }

        val DataProducer<*>.verboseName: String
            get() = "$scope - $name"

        /**
         * Command to execute.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val subCommands = listOf(
                KafkaTopicsCommand(),
                SchemaRegistryCommand(),
                ListCommand(),
                ValidatorCommand(),
            ).sortedBy { it.name }

            val parser = getArgumentParser(subCommands)

            val ns: Namespace = try {
                parser.parseArgs(args)
            } catch (e: HelpScreenException) {
                parser.printHelp()
                exitProcess(0)
            } catch (e: ArgumentParserException) {
                parser.handleError(e)
                exitProcess(1)
            }

            processLoggingOptions(ns)

            val root = Paths.get(ns.getString("root")).toAbsolutePath()
            val toolConfig = loadConfig(ns.getString("config"))

            logger.info("Loading radar-schemas-tools with configuration {}", toolConfig)
            runBlocking {
                val app: CommandLineApp = try {
                    val catalogue = SourceCatalogue(root, toolConfig.schemas, toolConfig.sources)
                    CommandLineApp(root, toolConfig, catalogue)
                } catch (e: IOException) {
                    logger.error("Failed to load catalog from root.")
                    exitProcess(1)
                }
                val subparser = ns.getString("subparser")
                val command = subCommands.find { it.name == subparser }
                    ?: run {
                        parser.handleError(
                            ArgumentParserException(
                                "Subcommand $subparser not implemented",
                                parser,
                            ),
                        )
                        exitProcess(1)
                    }
                exitProcess(command.execute(ns, app))
            }
        }

        private fun loadConfig(fileName: String): ToolConfig = try {
            loadToolConfig(fileName)
        } catch (ex: IOException) {
            logger.error(
                "Cannot configure radar-schemas-tools client from config file {}: {}",
                fileName,
                ex.message,
            )
            exitProcess(1)
        }

        private fun processLoggingOptions(ns: Namespace) {
            if (ns.getBoolean("verbose") == true) {
                Configurator.setAllLevels(LogManager.getRootLogger().name, Level.DEBUG)
            }
            if (ns.getBoolean("quiet") == true) {
                Configurator.setAllLevels(LogManager.getRootLogger().name, Level.ERROR)
            }
        }

        private fun getArgumentParser(subCommands: List<SubCommand>): ArgumentParser {
            val parser = ArgumentParsers.newFor("radar-schemas-tools")
                .addHelp(true)
                .build()
                .description("Schema tools")
            val subParsers = parser.addSubparsers().dest("subparser")
            for (command in subCommands) {
                command.addParser(subParsers.addParser(command.name, true))
            }
            return parser
        }
    }
}
