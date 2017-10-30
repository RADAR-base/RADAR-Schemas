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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.stream.StreamGroup;
import org.radarcns.schema.specification.monitor.MonitorSource;
import org.radarcns.schema.specification.passive.PassiveSource;
import org.radarcns.topic.AvroTopic;
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
import java.util.stream.Stream;

/**
 * TODO.
 */
public class SourceCatalogue {
    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogue.class);

    /** Folder names. */
    public static final String YAML_EXTENSION = ".yml";

    public static final Path BASE_PATH = Paths.get("../..").toAbsolutePath().normalize();

    private final Map<String, ActiveSource<?>> activeSources;
    private final Map<String, MonitorSource> monitorSources;
    private final Map<String, PassiveSource> passiveSources;
    private final Map<String, StreamGroup> streamGroups;

    private final Set<DataProducer<?>> sources;

    @SuppressWarnings("WeakerAccess")
    SourceCatalogue(Map<String, ActiveSource<?>> activeSources,
            Map<String, MonitorSource> monitorSources,
            Map<String, PassiveSource> passiveSources,
            Map<String, StreamGroup> streamGroups) {
        this.activeSources = activeSources;
        this.monitorSources = monitorSources;
        this.passiveSources = passiveSources;
        this.streamGroups = streamGroups;

        sources = new HashSet<>();

        sources.addAll(activeSources.values());
        sources.addAll(monitorSources.values());
        sources.addAll(passiveSources.values());
        sources.addAll(streamGroups.values());
    }

    public static SourceCatalogue load(Path root) throws IOException {
        Path specRoot = root.resolve("specifications");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        // only serialize fields, not getters, etc.
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        return new SourceCatalogue(
            initSources(mapper.readerFor(ActiveSource.class), specRoot, Scope.ACTIVE),
            initSources(mapper.readerFor(MonitorSource.class), specRoot, Scope.MONITOR),
            initSources(mapper.readerFor(PassiveSource.class), specRoot, Scope.PASSIVE),
            initSources(mapper.readerFor(StreamGroup.class), specRoot, Scope.STREAM));
    }

    private static <T> Map<String, T> initSources(ObjectReader reader, Path root, Scope scope)
            throws IOException {
        Path baseFolder = scope.getPath(root);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return Collections.emptyMap();
        }

        return Files.walk(baseFolder)
                .filter(Files::isRegularFile)
                .map(f -> {
                    try {
                        return new AbstractMap.SimpleImmutableEntry<>(
                                f.getFileName().toString()
                                        .split("\\.")[0]
                                        .toUpperCase(Locale.ENGLISH),
                                reader.<T>readValue(f.toFile()));
                    } catch (IOException ex) {
                        logger.error("Failed to load configuration {}", f, ex);
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
    public Map<String, ActiveSource<?>> getActiveSources() {
        return activeSources;
    }

    /**
     * TODO.
     * @param topic TODO
     * @return TODO
     */
    public ActiveSource getActiveSource(String topic) {
        return activeSources.get(topic);
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
     * @param topic TODO
     * @return TODO
     */
    public MonitorSource getMonitorSource(String topic) {
        return monitorSources.get(topic);
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
     * @param topic TODO
     * @return TODO
     */
    public PassiveSource getPassiveSource(String topic) {
        return passiveSources.get(topic);
    }

    public Map<String, StreamGroup> getStreamGroups() {
        return streamGroups;
    }

    public Set<DataProducer<?>> getSources() {
        return sources;
    }

    /**
     * TODO.
     * @return TODO
     */
    public Stream<String> getTopicNames() {
        return sources.stream()
                .flatMap(DataProducer::getTopicNames);
    }

    public Stream<AvroTopic<?, ?>> getTopics() {
        return sources.stream()
                .flatMap(DataProducer::getTopics);
    }
}
