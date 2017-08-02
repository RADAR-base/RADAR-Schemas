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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.radarcns.active.questionnaire.QuestionnaireType;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.specifications.util.Source;
import org.radarcns.specifications.util.active.ActiveSource;
import org.radarcns.specifications.util.active.QuestionnaireSource;
import org.radarcns.specifications.util.passive.MonitorSource;
import org.radarcns.specifications.util.passive.PassiveSource;
import org.radarcns.specifications.util.passive.Sensor;

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

    private static final String YAML_EXTENSION = ".yml";

    private static final Path BASE_PATH = Paths.get(new File(".").toURI())
            .getParent().getParent().getParent().resolve(BASE_FOLDER_NAME);

    private static final Map<QuestionnaireType, QuestionnaireSource> ACTIVE_SOURCES;
    private static final Map<MonitorSourceType, MonitorSource> MONITOR_SOURCES;
    private static final Map<PassiveSourceType, PassiveSource> PASSIVE_SOURCES;

    static {
        try {
            ACTIVE_SOURCES = initActiveSources();
            MONITOR_SOURCES = initMonitorSources();
            PASSIVE_SOURCES = initPassiveSources();
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

    public static Map<QuestionnaireType, QuestionnaireSource> getActiveSources() {
        return ACTIVE_SOURCES;
    }

    public static Map<MonitorSourceType, MonitorSource> getMonitorSources() {
        return MONITOR_SOURCES;
    }

    public static Map<PassiveSourceType, PassiveSource> getPassiveSources() {
        return PASSIVE_SOURCES;
    }
}
