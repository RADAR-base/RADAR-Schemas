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

import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.radarcns.active.questionnaire.QuestionnaireType;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.specifications.source.Source;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.specifications.source.passive.MonitorSource;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;

/**
 * TODO.
 */
public class SourceCatalogue {

    /** Folder names. */
    public enum NameFolder {
        ACTIVE("active"),
        MONITOR("monitor"),
        PASSIVE("passive");

        private final String name;

        NameFolder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final String BASE_FOLDER_NAME = "specifications";

    public static final String YAML_EXTENSION = ".yml";

    public static final Path BASE_PATH = Paths.get(new File(".").toURI())
            .getParent().getParent().getParent().resolve(BASE_FOLDER_NAME);

    private static final Map<QuestionnaireType, QuestionnaireSource> ACTIVE_SOURCES;
    private static final Map<MonitorSourceType, MonitorSource> MONITOR_SOURCES;
    private static final Map<PassiveSourceType, PassiveSource> PASSIVE_SOURCES;

    private static final Set<Source> SOURCES;

    static {
        try {
            ACTIVE_SOURCES = initActiveSources();
            MONITOR_SOURCES = initMonitorSources();
            PASSIVE_SOURCES = initPassiveSources();

            int initCapacity = (int) Math.ceil((ACTIVE_SOURCES.size()
                    + MONITOR_SOURCES.size() + PASSIVE_SOURCES.size()) * 100d / 75d);
            SOURCES = new HashSet<>(initCapacity);

            SOURCES.addAll(ACTIVE_SOURCES.values());
            SOURCES.addAll(MONITOR_SOURCES.values());
            SOURCES.addAll(PASSIVE_SOURCES.values());
        } catch (IOException exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    private static Map<QuestionnaireType, QuestionnaireSource> initActiveSources()
            throws IOException {
        Map<QuestionnaireType, QuestionnaireSource> map = new HashMap<>();

        Path baseFolder = BASE_PATH.resolve(NameFolder.ACTIVE.getName());

        for (QuestionnaireType questionnaire : QuestionnaireType.values()) {
            if (questionnaire.name().equals(QuestionnaireType.UNKNOWN.name())) {
                continue;
            }

            map.put(questionnaire, new YamlConfigLoader().load(new File(
                    baseFolder.resolve(questionnaire.name().toLowerCase().concat(
                        YAML_EXTENSION)).toUri()), QuestionnaireSource.class));
        }

        return map;
    }

    private static Map<MonitorSourceType, MonitorSource> initMonitorSources() throws IOException {
        Map<MonitorSourceType, MonitorSource> map = new HashMap<>();

        Path baseFolder = BASE_PATH.resolve(NameFolder.MONITOR.getName());

        for (MonitorSourceType source : MonitorSourceType.values()) {
            if (source.name().equals(MonitorSourceType.UNKNOWN.name())) {
                continue;
            }

            map.put(source, new YamlConfigLoader().load(new File(
                    baseFolder.resolve(source.name().toLowerCase().concat(YAML_EXTENSION)).toUri()),
                    MonitorSource.class));
        }

        return map;
    }

    private static Map<PassiveSourceType, PassiveSource> initPassiveSources() throws IOException {
        Map<PassiveSourceType, PassiveSource> map = new HashMap<>();

        Path baseFolder = BASE_PATH.resolve(NameFolder.PASSIVE.getName());

        for (PassiveSourceType source : PassiveSourceType.values()) {
            if (source.name().equals(PassiveSourceType.UNKNOWN.name())) {
                continue;
            }

            map.put(source, new YamlConfigLoader().load(new File(
                    baseFolder.resolve(source.name().toLowerCase().concat(YAML_EXTENSION)).toUri()),
                    PassiveSource.class));
        }

        return map;
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Map<QuestionnaireType, QuestionnaireSource> getActiveSources() {
        return ACTIVE_SOURCES;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public static QuestionnaireSource getActiveSource(QuestionnaireType type) {
        return ACTIVE_SOURCES.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Map<MonitorSourceType, MonitorSource> getMonitorSources() {
        return MONITOR_SOURCES;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public static MonitorSource getMonitorSource(MonitorSourceType type) {
        return MONITOR_SOURCES.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Map<PassiveSourceType, PassiveSource> getPassiveSources() {
        return PASSIVE_SOURCES;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public static PassiveSource getPassiveSource(PassiveSourceType type) {
        return PASSIVE_SOURCES.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Set<String> getTopics() {
        Set<String> set = new HashSet<>();
        SOURCES.forEach(source -> set.addAll(source.getTopics()));
        return set;
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Map<String, Map<String, Set<String>>> getTopicsVerbose() {
        Map<String, Map<String, Set<String>>> map = new HashMap<>();

        for (QuestionnaireSource source : ACTIVE_SOURCES.values()) {
            Map<String, Set<String>> details = new HashMap<>();
            details.put(source.getQuestionnaireType().name(), singleton(source.getTopic()));
            map.put(ActiveSourceType.QUESTIONNAIRE.name(), details);
        }

        for (MonitorSource source : MONITOR_SOURCES.values()) {
            Map<String, Set<String>> details = new HashMap<>();
            details.put(source.getType().name(), source.getTopics());
            map.put(NameFolder.MONITOR.getName().toUpperCase(), details);
        }

        for (PassiveSource source : PASSIVE_SOURCES.values()) {
            Map<String, Set<String>> details = new HashMap<>();
            for (Sensor sensor : source.getSensors()) {
                details.put(sensor.getName().name(), sensor.getTopics());
            }
            for (Processor proc : source.getProcessors()) {
                details.put(proc.getName().name(), proc.getTopics());
            }
            map.put(source.getType().name(), details);
        }

        return map;
    }
}
