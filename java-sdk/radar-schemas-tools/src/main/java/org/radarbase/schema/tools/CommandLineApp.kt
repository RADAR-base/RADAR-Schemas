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

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.radarbase.schema.specification.DataTopic
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.stream.StreamGroup
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream
import kotlin.system.exitProcess

/**
 * Command line app containing a source catalogue.
 * It starts at a RADAR-Schemas root. The source catalogue is read from the
 * `specifications` directory in that root.
 * @param root path to the root of a RADAR-Schemas directory.
 * @throws IOException if the source catalogue cannot be loaded.
 */
class CommandLineApp(val root: Path) {
    val catalogue: SourceCatalogue = SourceCatalogue.load(root)

    init {
        logger.info("radar-schema-tools is initialized with root directory {}", this.root)
    }

    val topicsToCreate: Stream<String>
        get() = catalogue.topicNames

    val rawTopics: Stream<String>
        get() = Stream.of(
            catalogue.passiveSources,
            catalogue.activeSources,
            catalogue.monitorSources,
            catalogue.connectorSources,
            catalogue.pushSources,
        )
            .flatMap { it.values.stream() }
            .flatMap { it.topicNames }

    val resultsCacheTopics: Stream<String>
        get() = catalogue.streamGroups.values.stream()
            .flatMap { obj: StreamGroup -> obj.timedTopicNames }

    fun getTopicsVerbose(prettyPrint: Boolean, source: String?): Stream<String> {
        return catalogue.sources.parallelStream()
            .filter { s ->
                (source == null
                    || source.equals("${s.scope} - ${s.name}", ignoreCase = true))
            }
            .map { s ->
                ("${s.scope} - ${s.name}\n"
                    + s.data.asSequence()
                    .sortedBy { it.topic }
                    .joinToString(separator = "\n") { t: DataTopic ->
                        var details = t.toString(prettyPrint)
                        details = t.toString(prettyPrint)
                            .substring(0, details.length - 1)
                            .replace("\n", "\n    ")
                        "  ${t.topic}\n    $details"
                    })
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CommandLineApp::class.java)

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
                SchemaTopicManagerCommand(),
            ).associateByTo(TreeMap()) { it.name }
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

            val isVerbose = ns.getBoolean("verbose")
            if (isVerbose == true) {
                Configurator.setAllLevels(LogManager.getRootLogger().name, Level.DEBUG)
            }
            val isQuiet = ns.getBoolean("quiet")
            if (isQuiet == true) {
                Configurator.setAllLevels(LogManager.getRootLogger().name, Level.ERROR)
            }
            val app: CommandLineApp = try {
                CommandLineApp(Paths.get(ns.getString("root")).toAbsolutePath())
            } catch (e: IOException) {
                logger.error("Failed to load catalog from root.")
                exitProcess(1)
            }
            val command = subCommands[ns.getString("subparser")]
            if (command == null) {
                parser.handleError(ArgumentParserException(
                    "Subcommand " + ns.getString("subparser") + " not implemented",
                    parser))
            } else {
                exitProcess(command.execute(ns, app))
            }
        }

        private fun getArgumentParser(subCommands: SortedMap<String, SubCommand>): ArgumentParser {
            val parser = ArgumentParsers.newFor("radar-schemas-tools")
                .addHelp(true)
                .build()
                .description("Schema tools")
            val subParsers = parser.addSubparsers().dest("subparser")
            for (command in subCommands.values) {
                command.addParser(subParsers.addParser(command.name, true))
            }
            return parser
        }
    }
}
