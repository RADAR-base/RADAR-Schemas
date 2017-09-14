package org.radarcns.schema;

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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.radarcns.schema.specification.KafkaActor;
import org.radarcns.schema.specification.MonitorSource;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.Topic;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.active.questionnaire.QuestionnaireSource;
import org.radarcns.schema.specification.passive.Processor;
import org.radarcns.schema.specification.passive.Sensor;
import org.radarcns.schema.specification.passive.PassiveSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public class CommandLineApp {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineApp.class);

    private final SourceCatalogue catalogue;

    public CommandLineApp(Path root) throws IOException {
        this.catalogue = SourceCatalogue.load(root);
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getTopicsToCreate() {
        Set<String> set = new HashSet<>();

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
        StringBuilder result = new StringBuilder();

        Map<String, Map<String, String>> map = getTopicsInfoVerbose(reduced);

        List<String> rootKeys = new ArrayList<>(map.keySet());
        Collections.sort(rootKeys);

        for (String key : rootKeys) {
            if (Objects.isNull(source) || key.equalsIgnoreCase(source)) {
                result.append(key).append('\n');

                List<String> firstLevelKeys = new ArrayList<>(map.get(key).keySet());
                Collections.sort(firstLevelKeys);

                for (String details : firstLevelKeys) {
                    result.append('\t').append(details);
                    result.append("\n\t\t").append(map.get(key).get(details));
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

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("Usage: app ROOT_DIRECTORY");
            System.exit(1);
        }
        CommandLineApp app = null;
        try {
            app = new CommandLineApp(Paths.get(args[0]));
        } catch (IOException e) {
            logger.error("Failed to load catalog from first argument.");
            System.exit(1);
        }
        System.out.println(app.getTopicsToCreate());
    }

}
