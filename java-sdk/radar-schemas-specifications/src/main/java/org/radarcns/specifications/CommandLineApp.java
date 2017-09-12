package org.radarcns.specifications;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.specifications.SourceCatalogue.NameFolder;
import org.radarcns.specifications.source.KafkaActor;
import org.radarcns.specifications.source.Topic;
import org.radarcns.specifications.source.Topic.TopicMetadata;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.specifications.source.passive.MonitorSource;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;

/**
 * TODO.
 */
public class CommandLineApp {

    /**
     * TODO.
     * @return TODO
     */
    public static Set<String> getTopicsToCreate() {
        Set<String> set = new HashSet<>();

        for (Topic topic : getAllTopics()) {
            set.add(topic.getInputTopic());
            if (topic.hasAggregator()) {
                topic.getOutputTopics().stream()
                    .map(TopicMetadata::getOutput)
                    .forEach(set::add);
            }
        }

        return set;
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Set<String> getRawTopics() {
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
    public static Set<String> getResultsCacheTopics() {
        Set<String> set = new HashSet<>();

        for (Topic topic : getAllTopics()) {
            if (topic.hasAggregator()) {
                topic.getOutputTopics().stream()
                    .map(TopicMetadata::getOutput)
                    .forEach(set::add);
            } else {
                set.add(topic.getInputTopic());
            }
        }

        return set;
    }

    private static Set<Topic> getAllTopics() {
        Set<Topic> set = new HashSet<>();

        SourceCatalogue.getActiveSources().values().stream()
            .map(QuestionnaireSource::getTopic)
            .forEach(set::add);

        SourceCatalogue.getMonitorSources().values().stream()
            .map(MonitorSource::getKafkaActor)
            .map(KafkaActor::getTopic)
            .forEach(set::add);

        SourceCatalogue.getPassiveSources().values().stream()
            .map(PassiveSource::getSensors)
            .flatMap(Set::stream)
            .map(Sensor::getTopic)
            .forEach(set::add);

        SourceCatalogue.getPassiveSources().values().stream()
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
    public static String getTopicsVerbose(boolean reduced, String source) {
        String result = "";

        Map<String, Map<String, String>> map = getTopicsInfoVerbose(reduced);

        List<String> rootKeys = new ArrayList<>(map.keySet());
        Collections.sort(rootKeys);

        for (String key : rootKeys) {
            if (Objects.isNull(source) || key.equalsIgnoreCase(source)) {
                result = result.concat(key).concat("\n");

                List<String> firstLevelKeys = new ArrayList<>(map.get(key).keySet());
                Collections.sort(firstLevelKeys);

                for (String details : firstLevelKeys) {
                    result = result.concat("\t").concat(details).concat("\n");
                    result = result.concat("\t\t").concat(map.get(key).get(details));
                }
                result = result.concat("\n");
            }
        }

        return result;
    }

    /**
     * TODO.
     * @param reduced TODO
     * @return TODO
     */
    private static Map<String, Map<String, String>> getTopicsInfoVerbose(boolean reduced) {
        Map<String, Map<String, String>> map = new HashMap<>();

        Map<String, String> details = new HashMap<>();
        for (QuestionnaireSource source : SourceCatalogue.getActiveSources().values()) {
            details.put(source.getQuestionnaireType().name(), source.getTopic().toString(reduced));
        }
        map.put(ActiveSourceType.QUESTIONNAIRE.name(), details);

        details = new HashMap<>();
        for (MonitorSource source : SourceCatalogue.getMonitorSources().values()) {
            details.put(source.getType(),
                    source.getKafkaActor().getTopic().toString(reduced));
        }
        map.put(NameFolder.MONITOR.getName().toUpperCase(), details);

        for (PassiveSource source : SourceCatalogue.getPassiveSources().values()) {
            details = new HashMap<>();
            for (Sensor sensor : source.getSensors()) {
                details.put(sensor.getName().name(), sensor.getTopic().toString(reduced));
            }
            for (Processor proc : source.getProcessors()) {
                details.put(proc.getName(), proc.getTopic().toString(reduced));
            }
            map.put(source.getType().name(), details);
        }

        return map;
    }

}
