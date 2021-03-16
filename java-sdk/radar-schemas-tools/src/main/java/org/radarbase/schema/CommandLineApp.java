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

package org.radarbase.schema;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.radarbase.schema.registration.ConfluentCloudTopics;
import org.radarbase.schema.registration.KafkaTopics;
import org.radarbase.schema.registration.SchemaTopicManager;
import org.radarbase.schema.registration.SchemaRegistry;
import org.radarbase.schema.service.SourceCatalogueServer;
import org.radarbase.schema.specification.DataProducer;
import org.radarbase.schema.specification.DataTopic;
import org.radarbase.schema.specification.SourceCatalogue;
import org.radarbase.schema.specification.stream.StreamGroup;
import org.radarbase.schema.util.SubCommand;
import org.radarbase.schema.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line app containing a source catalogue.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.DoNotCallSystemExit"})
public class CommandLineApp {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineApp.class);

    private final SourceCatalogue catalogue;
    private final Path root;

    /**
     * Command line app started at a RADAR-Schemas root. The source catalogue is read from the
     * {@code specifications} directory in that root.
     * @param root path to the root of a RADAR-Schemas directory.
     * @throws IOException if the source catalogue cannot be loaded.
     */
    public CommandLineApp(Path root) throws IOException {
        this.root = root;
        this.catalogue = SourceCatalogue.load(root);
        logger.info("radar-schema-tools is initialized with root directory {}", this.root);
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public Stream<String> getTopicsToCreate() {
        return catalogue.getTopicNames();
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public Stream<String> getRawTopics() {
        return Stream.of(
                catalogue.getPassiveSources(),
                catalogue.getActiveSources(),
                catalogue.getMonitorSources(),
                catalogue.getConnectorSources(),
                catalogue.getPushSources())
                .flatMap(map -> map.values().stream())
                .flatMap(DataProducer::getTopicNames);
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public Stream<String> getResultsCacheTopics() {
        return catalogue.getStreamGroups().values().stream()
                .flatMap(StreamGroup::getTimedTopicNames);
    }

    public SourceCatalogue getCatalogue() {
        return catalogue;
    }

    public Path getRoot() {
        return root;
    }

    /**
     * TODO.
     *
     * @param prettyPrint TODO
     * @return TODO
     */
    public Stream<String> getTopicsVerbose(boolean prettyPrint, String source) {
        return catalogue.getSources().parallelStream()
                .filter(s -> source == null
                        || source.equalsIgnoreCase(s.getScope() + " - " + s.getName()))
                .map(s -> s.getScope() + " - " + s.getName() + "\n"
                        + s.getData().stream()
                                .sorted(Comparator.comparing(DataTopic::getTopic))
                                .map(t -> {
                                    String details = t.toString(prettyPrint);
                                    details = details.substring(0, details.length() - 1)
                                            .replace("\n", "\n    ");
                                    return "  " + t.getTopic() + "\n    " + details;
                                })
                                .collect(Collectors.joining("\n")));
    }

    /** Command to execute. */
    public static void main(String... args) {
        SortedMap<String, SubCommand> subCommands = commandsToMap(
                KafkaTopics.command(),
                ConfluentCloudTopics.command(),
                SchemaRegistry.command(),
                listCommand(),
                SchemaValidator.command(),
                SourceCatalogueServer.command(),
                SchemaTopicManager.command());

        ArgumentParser parser = getArgumentParser(subCommands);

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        if (ns.getBoolean("help") != null && ns.getBoolean("help")) {
            parser.printHelp();
            System.exit(0);
        }

        CommandLineApp app = null;
        try {
            app = new CommandLineApp(Paths.get(ns.getString("root")).toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to load catalog from root.");
            System.exit(1);
        }

        SubCommand command = subCommands.get(ns.getString("subparser"));
        if (command == null) {
            parser.handleError(new ArgumentParserException(
                    "Subcommand " + ns.getString("subparser") + " not implemented",
                    parser));
        } else {
            System.exit(command.execute(ns, app));
        }
    }

    private static ArgumentParser getArgumentParser(SortedMap<String, SubCommand> subCommands) {
        ArgumentParser parser = ArgumentParsers.newFor("radar-schemas-tools")
                .addHelp(true)
                .build()
                .description("Schema tools");

        Subparsers subParsers = parser.addSubparsers().dest("subparser");

        for (SubCommand command : subCommands.values()) {
            command.addParser(subParsers.addParser(command.getName(), true));
        }

        return parser;
    }

    private static SortedMap<String, SubCommand> commandsToMap(SubCommand... commands) {
        SortedMap<String, SubCommand> map = new TreeMap<>();
        for (SubCommand sub : commands) {
            map.put(sub.getName(), sub);
        }
        return map;
    }

    /** Command to list the topics. */
    private static SubCommand listCommand() {
        return new SubCommand() {
            @Override
            public String getName() {
                return "list";
            }

            @Override
            public int execute(Namespace options, CommandLineApp app) {
                Stream<String> out;
                if (options.getBoolean("raw")) {
                    out = app.getRawTopics();
                } else if (options.getBoolean("stream")) {
                    out = app.getResultsCacheTopics();
                } else if (options.getBoolean("quiet")) {
                    out = app.getTopicsToCreate();
                } else {
                    out = app.getTopicsVerbose(true, options.getString("match"));
                }
                System.out.println(out.sorted().distinct()
                        .collect(Collectors.joining("\n")));
                return 0;
            }

            @Override
            public void addParser(ArgumentParser parser) {
                parser.description("list topics and schemas");
                parser.addArgument("-r", "--raw")
                        .help("list raw input topics")
                        .action(Arguments.storeTrue());
                parser.addArgument("-q", "--quiet")
                        .help("only print the requested topics")
                        .action(Arguments.storeTrue());
                parser.addArgument("-m", "--match")
                        .help("only print the requested topics");
                parser.addArgument("-S", "--stream")
                        .help("list the output topics of Kafka Streams")
                        .action(Arguments.storeTrue());
                SubCommand.addRootArgument(parser);
            }
        };
    }

    /**
     * Create a pattern to match given topic. If the exact match is non-null, it is returned
     * as an exact match, otherwise if regex is non-null, it is used, and otherwise
     * {@code null} is returned.
     * @param exact string that should be exactly matched.
     * @param regex string that should be matched as a regex.
     * @return pattern or {@code null} if both exact and regex are {@code null}.
     */
    public static Pattern matchTopic(String exact, String regex) {
        if (exact != null) {
            return Pattern.compile("^" + Pattern.quote(exact) + "$");
        } else if (regex != null) {
            return Pattern.compile(regex);
        } else {
            return null;
        }
    }
}
