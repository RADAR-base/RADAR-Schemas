package org.radarcns.schema.specification;

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

import org.radarcns.config.YamlConfigLoader;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.source.Source;
import org.radarcns.schema.specification.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.schema.specification.source.MonitorSource;
import org.radarcns.schema.specification.source.passive.PassiveSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO.
 */
public class SourceCatalogue {
    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogue.class);

    /** Folder names. */
    public static final String YAML_EXTENSION = ".yml";

    public static final Path BASE_PATH = Paths.get("../..");

    private final Map<String, QuestionnaireSource> activeSources;
    private final Map<String, MonitorSource> monitorSources;
    private final Map<String, PassiveSource> passiveSources;

    private final Set<Source> sources;

    // package private for testing
    SourceCatalogue(Map<String, QuestionnaireSource> activeSources,
            Map<String, MonitorSource> monitorSources,
            Map<String, PassiveSource> passiveSources) {
        this.activeSources = activeSources;
        this.monitorSources = monitorSources;
        this.passiveSources = passiveSources;

        sources = new HashSet<>();

        sources.addAll(activeSources.values());
        sources.addAll(monitorSources.values());
        sources.addAll(passiveSources.values());
    }

    public static SourceCatalogue load(Path root) throws IOException {
        Path specRoot = root.resolve("specifications");
        return new SourceCatalogue(
            initSources(specRoot, Scope.ACTIVE, QuestionnaireSource.class),
            initSources(specRoot, Scope.MONITOR, MonitorSource.class),
            initSources(specRoot, Scope.PASSIVE, PassiveSource.class)
        );
    }

    private static <T> Map<String, T> initSources(Path root, Scope scope, Class<T> sourceClass)
            throws IOException {
        Path baseFolder = scope.getPath(root);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return Collections.emptyMap();
        }

        YamlConfigLoader configLoader = new YamlConfigLoader();

        return Files.walk(baseFolder)
                .filter(Files::isRegularFile)
                .map(f -> {
                    try {
                        return new AbstractMap.SimpleImmutableEntry<>(
                                f.getFileName().toString()
                                        .split("\\.")[0]
                                        .toUpperCase(Locale.ENGLISH),
                                configLoader.load(f.toFile(), sourceClass));
                    } catch (IOException e) {
                        logger.error("Failed to load configuration", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * TODO.
     * @return TODO
     */
    public Map<String, QuestionnaireSource> getActiveSources() {
        return activeSources;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public QuestionnaireSource getActiveSource(String type) {
        return activeSources.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public Map<String, MonitorSource> getMonitorSources() {
        return monitorSources;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public MonitorSource getMonitorSource(String type) {
        return monitorSources.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public Map<String, PassiveSource> getPassiveSources() {
        return passiveSources;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public PassiveSource getPassiveSource(String type) {
        return passiveSources.get(type);
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getTopics() {
        Set<String> set = new HashSet<>();
        sources.forEach(source -> set.addAll(source.getTopics()));
        return set;
    }
}
