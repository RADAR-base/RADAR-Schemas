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
import org.radarcns.schema.specification.KafkaActor;
import org.radarcns.schema.specification.MonitorSource;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.Topic;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.active.questionnaire.QuestionnaireSource;
import org.radarcns.schema.specification.passive.PassiveSource;
import org.radarcns.schema.specification.passive.Processor;
import org.radarcns.schema.specification.passive.Sensor;
import org.radarcns.schema.validation.SchemaValidator;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.config.ExcludeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TODO.
 */
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
    public SortedSet<String> getTopicsToCreate() {
        SortedSet<String> set = new TreeSet<>();

        for (Topic topic : getAllTopics()) {
            set.add(topic.getInputTopic());
            if (topic.hasAggregator()) {
                topic.getOutputTopics().stream()
                    .map(Topic.TopicMetadata::getOutput)
                    .forEach(set::add);
            }
        }

        return set;
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getRawTopics() {
        Set<String> set = new HashSet<>();

        for (Topic topic : getAllTopics()) {
            set.add(topic.getInputTopic());
        }

        return set;
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getResultsCacheTopics() {
        Set<String> set = new HashSet<>();

        for (Topic topic : getAllTopics()) {
            if (topic.hasAggregator()) {
                topic.getOutputTopics().stream()
                    .map(Topic.TopicMetadata::getOutput)
                    .forEach(set::add);
            } else {
                set.add(topic.getInputTopic());
            }
        }

        return set;
    }

    private Set<Topic> getAllTopics() {
        Set<Topic> set = new HashSet<>();

        catalogue.getActiveSources().values().stream()
            .map(ActiveSource::getTopic)
            .forEach(set::add);

        catalogue.getMonitorSources().values().stream()
            .map(MonitorSource::getKafkaActor)
            .map(KafkaActor::getTopic)
            .forEach(set::add);

        catalogue.getPassiveSources().values().stream()
            .map(PassiveSource::getSensors)
            .flatMap(Set::stream)
            .map(Sensor::getTopic)
            .forEach(set::add);

        catalogue.getPassiveSources().values().stream()
            .map(PassiveSource::getProcessors)
            .flatMap(Set::stream)
            .map(Processor::getTopic)
            .forEach(set::add);

        return set;
    }

    /**
     * TODO.
     * @param reduced TODO
     * @return TODO
     */
    public String getTopicsVerbose(boolean reduced, String source) {
        logger.info("Topic list {} {}", reduced, source);
        StringBuilder result = new StringBuilder();

        Map<String, Map<String, String>> map = getTopicsInfoVerbose(reduced);

        List<String> rootKeys = new ArrayList<>(map.keySet());
        Collections.sort(rootKeys);

        for (String key : rootKeys) {
            if (source == null || key.equalsIgnoreCase(source)) {
                result.append(key).append('\n');

                List<String> firstLevelKeys = new ArrayList<>(map.get(key).keySet());
                Collections.sort(firstLevelKeys);

                for (String details : firstLevelKeys) {
                    result.append('\t').append(details);
                    result.append("\n\t\t")
                            .append(map.get(key).get(details)
                                    .replace("\n", "\n\t\t"));
                }
                result.append('\n');
            }
        }

        return result.toString();
    }

    /**
     * TODO.
     * @param reduced TODO
     * @return TODO
     */
    private Map<String, Map<String, String>> getTopicsInfoVerbose(boolean reduced) {
        Map<String, Map<String, String>> map = new HashMap<>();

        Map<String, String> details = new HashMap<>();
        for (ActiveSource source : catalogue.getActiveSources().values()) {
            if (source instanceof QuestionnaireSource) {
                details.put(
                        ((QuestionnaireSource)source).getQuestionnaireType(),
                        source.getTopic().toString(reduced));
            }
        }
        map.put(ActiveSource.RadarSourceTypes.QUESTIONNAIRE.name(), details);

        details = new HashMap<>();
        for (MonitorSource source : catalogue.getMonitorSources().values()) {
            details.put(source.getType(),
                    source.getKafkaActor().getTopic().toString(reduced));
        }
        map.put(Scope.MONITOR.name(), details);

        for (PassiveSource source : catalogue.getPassiveSources().values()) {
            details = new HashMap<>();
            for (Sensor sensor : source.getSensors()) {
                details.put(sensor.getName().name(), sensor.getTopic().toString(reduced));
            }
            for (Processor proc : source.getProcessors()) {
                details.put(proc.getName(), proc.getTopic().toString(reduced));
            }
            map.put(source.getType(), details);
        }

        return map;
    }


    public String validateSchemas(String scopeString, String configSubPath) throws IOException {
        Path configPath = null;
        if (configSubPath != null) {
            if (configSubPath.charAt(0) == '/') {
                configPath = Paths.get(configSubPath);
            } else {
                configPath = root.resolve(configSubPath);
            }
        }
        ExcludeConfig config = ExcludeConfig.load(configPath);
        SchemaValidator validator = new SchemaValidator(root, config);
        StringBuilder result = new StringBuilder();
        if (scopeString == null) {
            for (Scope scope : Scope.values()) {
                Collection<ValidationException> results = validator.analyseFiles(scope);
                for (ValidationException ex : results) {
                    result.append("Validation FAILED:\n")
                            .append(ex.getMessage()).append("\n\n");
                }
            }
        } else {
            Scope scope = Scope.valueOf(scopeString);
            Collection<ValidationException> results = validator.analyseFiles(scope);
            for (ValidationException ex : results) {
                result.append("Validation FAILED:\n")
                        .append(ex.getMessage()).append("\n\n");
            }
        }
        return result.toString();
    }

    @SuppressWarnings("PMD.SystemPrintln")
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
                    System.out.println(app.getTopicsVerbose(false, ns.getString("match")));
                }
                break;
            case "validate":
                try {
                    System.out.println(app.validateSchemas(
                            ns.getString("scope"), ns.getString("config")));
                } catch (IOException e) {
                    logger.error("Failed to load schemas", e);
                }
                break;
            default:
                parser.handleError(new ArgumentParserException(
                        "Subcommand " + ns.getString("subparser") + " not implemented",
                        parser));
                break;
        }
    }
}
