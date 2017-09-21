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
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.stream.StreamGroup;
import org.radarcns.schema.validation.SchemaValidator;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.config.ExcludeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @return TODO
     */
    public List<String> getTopicsToCreate() {
        return catalogue.getTopicNames()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * TODO.
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
                .collect(Collectors.toList());
    }

    /**
     * TODO.
     * @return TODO
     */
    public List<String> getResultsCacheTopics() {
        return catalogue.getStreamGroups().values().stream()
                .flatMap(StreamGroup::getTimedTopicNames)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * TODO.
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

    /**
     * TODO.
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

    public int validateSchemas(Namespace ns) {
        try {
            ExcludeConfig config = loadConfig(ns.getString("config"));
            SchemaValidator validator = new SchemaValidator(root, config);
            Stream<ValidationException> stream = validateSchemas(
                    ns.getString("scope"), validator);

            if (ns.getBoolean("quiet")) {
                return stream.count() > 0 ? 1 : 0;
            } else {
                String result = SchemaValidator.format(stream);

                System.out.println(result);
                if (ns.getBoolean("verbose")) {
                    System.out.println("Validated schemas:");
                    for (String name : new TreeSet<>(validator.getValidatedSchemas().keySet())) {
                        System.out.println(" - " + name);
                    }
                    System.out.println();
                }
                return result.isEmpty() ? 0 : 1;
            }
        } catch (IOException e) {
            System.err.println("Failed to load schemas: " + e);
            return 1;
        }
    }

    private Stream<ValidationException> validateSchemas(String scopeString,
            SchemaValidator validator) throws IOException {
        if (scopeString == null) {
            return validator.analyseFiles();
        } else {
            return validator.analyseFiles(Scope.valueOf(scopeString));
        }
    }

    private ExcludeConfig loadConfig(String configSubPath) throws IOException {
        Path configPath = null;
        if (configSubPath != null) {
            if (configSubPath.charAt(0) == '/') {
                configPath = Paths.get(configSubPath);
            } else {
                configPath = root.resolve(configSubPath);
            }
        }
        return ExcludeConfig.load(configPath);
    }

    public static void main(String... args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("radar-schema")
                .defaultHelp(true)
                .description("Validate and list schema specifications");

        Subparsers subParsers = parser.addSubparsers().dest("subparser");
        Subparser validateParser = subParsers.addParser("validate", true)
                .description("Validate a set of specifications");
        validateParser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
        validateParser.addArgument("-s", "--scope")
                .help("Type of specifications to validate")
                .choices(Scope.values());
        validateParser.addArgument("-c", "--config")
                .help("Configuration file to use");
        validateParser.addArgument("-v", "--verbose")
                .help("Verbose validation message")
                .action(Arguments.storeTrue());
        validateParser.addArgument("-q", "--quiet")
                .help("Only set exit code.")
                .action(Arguments.storeTrue());

        Subparser listParser = subParsers.addParser("list", true)
                .description("list topics and schemas");
        listParser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
        listParser.addArgument("-r", "--raw")
                .help("List raw input topics")
                .action(Arguments.storeTrue());
        listParser.addArgument("-q", "--quiet")
                .help("Only print the requested topics")
                .action(Arguments.storeTrue());
        listParser.addArgument("-m", "--match")
                .help("Only print the requested topics");
        listParser.addArgument("-S", "--stream")
                .help("List the output topics of Kafka Streams")
                .action(Arguments.storeTrue());

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
        switch (ns.getString("subparser")) {
            case "list":
                if (ns.getBoolean("raw")) {
                    System.out.println(String.join("\n", app.getRawTopics()));
                } else if (ns.getBoolean("stream")) {
                    System.out.println(String.join("\n", app.getResultsCacheTopics()));
                } else if (ns.getBoolean("quiet")) {
                    System.out.println(String.join("\n", app.getTopicsToCreate()));
                } else {
                    System.out.println(app.getTopicsVerbose(true, ns.getString("match")));
                }
                break;
            case "validate":
                System.exit(app.validateSchemas(ns));
                break;
            default:
                parser.handleError(new ArgumentParserException(
                        "Subcommand " + ns.getString("subparser") + " not implemented",
                        parser));
                break;
        }
    }
}
