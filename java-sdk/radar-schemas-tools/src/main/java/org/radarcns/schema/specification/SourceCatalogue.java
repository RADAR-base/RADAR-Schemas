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

package org.radarcns.schema.specification;

import org.radarcns.config.YamlConfigLoader;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.active.questionnaire.QuestionnaireSource;
import org.radarcns.schema.specification.passive.PassiveSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO.
 */
public class SourceCatalogue {
    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogue.class);

    /** Folder names. */
    public static final String YAML_EXTENSION = ".yml";

    public static final Path BASE_PATH = Paths.get("../..");

    private final Map<String, ActiveSource> activeSources;
    private final Map<String, MonitorSource> monitorSources;
    private final Map<String, PassiveSource> passiveSources;

    private final Set<Source> sources;

    @SuppressWarnings("WeakerAccess")
    SourceCatalogue(Map<String, ActiveSource> activeSources,
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
        YamlConfigLoader configLoader = new YamlConfigLoader();

        return new SourceCatalogue(
            initActiveSources(configLoader, specRoot),
            initSources(configLoader, specRoot, Scope.MONITOR, MonitorSource.class),
            initSources(configLoader, specRoot, Scope.PASSIVE, PassiveSource.class)
        );
    }

    private static <T> Map<String, T> initSources(YamlConfigLoader configLoader, Path root,
            Scope scope, Class<T> sourceClass) throws IOException {
        return initSources(root, scope, f -> {
            try {
                return configLoader.load(f.toFile(), sourceClass);
            } catch (IOException e) {
                logger.error("Failed to load configuration {}", f, e);
                return null;
            }
        });
    }

    private static <T> Map<String, T> initSources(Path root, Scope scope, Function<Path, T> map)
            throws IOException {
        Path baseFolder = scope.getPath(root);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return Collections.emptyMap();
        }

        return Files.walk(baseFolder)
                .filter(Files::isRegularFile)
                .map(f -> new AbstractMap.SimpleImmutableEntry<>(
                        f.getFileName().toString()
                                .split("\\.")[0]
                                .toUpperCase(Locale.ENGLISH),
                        map.apply(f)))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, ActiveSource> initActiveSources(YamlConfigLoader configLoader,
            Path root) throws IOException {
        return initSources(root, Scope.ACTIVE, f -> {
            try {
                File file = f.toFile();
                ActiveSource source = configLoader.load(file, ActiveSource.class);
                switch (source.getAssessmentType().toUpperCase(Locale.ENGLISH)) {
                    case "QUESTIONNAIRE":
                        return configLoader.load(file, QuestionnaireSource.class);
                    default:
                        return source;
                }
            } catch (IOException e) {
                logger.error("Failed to load configuration {}", f, e);
                return null;
            }
        });
    }

    /**
     * TODO.
     * @return TODO
     */
    public Map<String, ActiveSource> getActiveSources() {
        return activeSources;
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public ActiveSource getActiveSource(String type) {
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
        return sources.stream()
                .flatMap(source -> source.getTopics().stream())
                .collect(Collectors.toSet());
    }
}
