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
import org.radarcns.schema.registration.KafkaTopics;
import org.radarcns.schema.registration.SchemaRegistry;
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
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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

    public int registerSchemas(String url, boolean force) {
        try (SchemaRegistry registration = new SchemaRegistry(url)) {
            boolean forced = force;
            if (forced) {
                forced = registration.setCompatibility(SchemaRegistry.Compatibility.NONE);
            }
            int result = registration.registerSchemas(catalogue) ? 0 : 1;
            if (forced) {
                registration.setCompatibility(SchemaRegistry.Compatibility.FULL);
            }
            return result;
        } catch (MalformedURLException ex) {
            logger.error("Schema registry URL {} is invalid: {}", url, ex.toString());
            return 1;
        }
    }

    public int createTopics(String zookeeper, int partitions, int replication) {
        try (KafkaTopics topics = new KafkaTopics(zookeeper)) {
            return topics.createTopics(catalogue, partitions, replication) ? 0 : 1;
        }
    }

    public static void main(String... args) {
        ArgumentParser parser = getArgumentParser();

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
                listTopics(ns, app);
                break;
            case "validate":
                System.exit(app.validateSchemas(ns));
                break;
            case "register":
                System.exit(app.registerSchemas(
                        ns.getString("url"), ns.getBoolean("force")));
                break;
            case "create":
                System.exit(app.createTopics(ns.getString("zookeeper"),
                        ns.getInt("partitions"), ns.getInt("replication")));
                break;
            default:
                parser.handleError(new ArgumentParserException(
                        "Subcommand " + ns.getString("subparser") + " not implemented",
                        parser));
                break;
        }
    }

    private static void listTopics(Namespace ns, CommandLineApp app) {
        if (ns.getBoolean("raw")) {
            System.out.println(String.join("\n", app.getRawTopics()));
        } else if (ns.getBoolean("stream")) {
            System.out.println(String.join("\n", app.getResultsCacheTopics()));
        } else if (ns.getBoolean("quiet")) {
            System.out.println(String.join("\n", app.getTopicsToCreate()));
        } else {
            System.out.println(app.getTopicsVerbose(true, ns.getString("match")));
        }
    }

    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("radar-schema")
                .defaultHelp(true)
                .description("Schema tools");

        Subparsers subParsers = parser.addSubparsers().dest("subparser");
        Subparser validateParser = subParsers.addParser("validate", true)
                .description("Validate a set of specifications.");
        addRootArgument(validateParser);
        validateParser.addArgument("-s", "--scope")
                .help("type of specifications to validate")
                .choices(Scope.values());
        validateParser.addArgument("-c", "--config")
                .help("configuration file to use");
        validateParser.addArgument("-v", "--verbose")
                .help("verbose validation message")
                .action(Arguments.storeTrue());
        validateParser.addArgument("-q", "--quiet")
                .help("only set exit code.")
                .action(Arguments.storeTrue());

        Subparser listParser = subParsers.addParser("list", true)
                .description("list topics and schemas");
        addRootArgument(listParser);
        listParser.addArgument("-r", "--raw")
                .help("list raw input topics")
                .action(Arguments.storeTrue());
        listParser.addArgument("-q", "--quiet")
                .help("only print the requested topics")
                .action(Arguments.storeTrue());
        listParser.addArgument("-m", "--match")
                .help("only print the requested topics");
        listParser.addArgument("-S", "--stream")
                .help("list the output topics of Kafka Streams")
                .action(Arguments.storeTrue());

        Subparser registerParser = subParsers.addParser("register", true)
                .description("Register schemas in the schema registry.");
        registerParser.addArgument("-f", "--force")
                .help("force registering schema, even if it is incompatible")
                .action(Arguments.storeTrue());
        registerParser.addArgument("schemaRegistry")
                .help("schema registry URL");
        addRootArgument(registerParser);


        Subparser createParser = subParsers.addParser("create", true)
                .description("Create all topics that are missing on the Kafka server.");
        createParser.addArgument("-p", "--partitions")
                .help("number of partitions per topic")
                .type(Integer.class)
                .setDefault(3);
        createParser.addArgument("-r", "--replication")
                .help("number of replicas per data packet")
                .type(Integer.class)
                .setDefault(3);
        createParser.addArgument("zookeeper")
                .help("zookeeper hosts and ports, comma-separated");
        addRootArgument(createParser);

        return parser;
    }

    private static void addRootArgument(ArgumentParser parser) {
        parser.addArgument("root")
                .nargs("?")
                .help("Root schemas directory with a specifications and commons directory")
                .setDefault(".");
    }
}
