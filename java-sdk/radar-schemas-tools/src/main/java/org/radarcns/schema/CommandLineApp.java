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

package org.radarcns.schema;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.radarcns.schema.registration.KafkaTopics;
import org.radarcns.schema.registration.SchemaRegistry;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.stream.StreamGroup;
import org.radarcns.schema.util.SubCommand;
import org.radarcns.schema.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * TODO.
 */
@SuppressWarnings("PMD.SystemPrintln")
public class CommandLineApp {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineApp.class);

    private final SourceCatalogue catalogue;
    private final Path root;

    public CommandLineApp(Path root) throws IOException {
        this.root = root;
        this.catalogue = SourceCatalogue.load(root);
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public List<String> getTopicsToCreate() {
        return catalogue.getTopicNames()
                .sorted()
                .collect(toList());
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public List<String> getRawTopics() {
        return Stream.of(
                catalogue.getPassiveSources(),
                catalogue.getActiveSources(),
                catalogue.getMonitorSources())
                .flatMap(map -> map.values().stream())
                .flatMap(DataProducer::getTopicNames)
                .sorted()
                .collect(toList());
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public List<String> getResultsCacheTopics() {
        return catalogue.getStreamGroups().values().stream()
                .flatMap(StreamGroup::getTimedTopicNames)
                .sorted()
                .collect(toList());
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public String getTopicsVerbose(boolean prettyPrint, String source) {
        logger.info("Topic list {} {}", prettyPrint, source);
        StringBuilder result = new StringBuilder();

        Map<String, Map<String, String>> map = getTopicsInfoVerbose(prettyPrint);

        List<String> rootKeys = new ArrayList<>(map.keySet());
        Collections.sort(rootKeys);

        for (String key : rootKeys) {
            if (source == null || key.equalsIgnoreCase(source)) {
                result.append(key).append('\n');

                List<String> firstLevelKeys = new ArrayList<>(map.get(key).keySet());
                Collections.sort(firstLevelKeys);

                for (String details : firstLevelKeys) {
                    result.append('\t').append(details);
                    result.append("\n\t\t");
                    String next = map.get(key).get(details)
                            .replace("\n", "\n\t\t");
                    // remove last two tabs
                    result.append(next.substring(0, next.length() - 2));
                }
                result.append('\n');
            }
        }

        return result.toString();
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
    private Map<String, Map<String, String>> getTopicsInfoVerbose(boolean prettyPrint) {
        return catalogue.getSources().stream()
                .collect(Collectors.toMap(
                        source -> source.getScope() + " - " + source.getName(),
                        source -> source.getData().stream()
                                .collect(Collectors.toMap(
                                        DataTopic::getTopic, d -> d.toString(prettyPrint)))));
    }

    public static void main(String... args) {
        SortedMap<String, SubCommand> subCommands = commandsToMap(
                KafkaTopics.command(),
                SchemaRegistry.command(),
                listCommand(),
                SchemaValidator.command());

        ArgumentParser parser = getArgumentParser(subCommands);

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        CommandLineApp app = null;
        try {
            app = new CommandLineApp(Paths.get(ns.getString("root")).toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to load catalog from root.");
            System.exit(1);
        }

        SubCommand command = subCommands.get(ns.getString("subparser"));
        if (command != null) {
            System.exit(command.execute(ns, app));
        } else {
            parser.handleError(new ArgumentParserException(
                    "Subcommand " + ns.getString("subparser") + " not implemented",
                    parser));
        }
    }

    private static ArgumentParser getArgumentParser(SortedMap<String, SubCommand> subCommands) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("radar-schema")
                .defaultHelp(true)
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

    private static SubCommand listCommand() {
        return new SubCommand() {
            @Override
            public String getName() {
                return "list";
            }

            @Override
            public int execute(Namespace options, CommandLineApp app) {
                if (options.getBoolean("raw")) {
                    System.out.println(String.join("\n", app.getRawTopics()));
                } else if (options.getBoolean("stream")) {
                    System.out.println(String.join("\n", app.getResultsCacheTopics()));
                } else if (options.getBoolean("quiet")) {
                    System.out.println(String.join("\n", app.getTopicsToCreate()));
                } else {
                    System.out.println(app.getTopicsVerbose(true,
                            options.getString("match")));
                }
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

    public static Pattern matchTopic(String exact, String regex) {
        if (exact != null) {
            return Pattern.compile("^" + Pattern.quote(exact) + "$");
        }
        if (regex != null) {
            return Pattern.compile(regex);
        }
        return null;
    }
}
