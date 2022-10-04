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

package org.radarbase.schema.specification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.radarbase.schema.SchemaCatalogue;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.active.ActiveSource;
import org.radarbase.schema.specification.config.SourceConfig;
import org.radarbase.schema.specification.connector.ConnectorSource;
import org.radarbase.schema.specification.monitor.MonitorSource;
import org.radarbase.schema.specification.passive.PassiveSource;
import org.radarbase.schema.specification.push.PushSource;
import org.radarbase.schema.specification.stream.StreamGroup;
import org.radarbase.topic.AvroTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public class SourceCatalogue {
    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogue.class);

    public static final Path BASE_PATH = Paths.get("../..").toAbsolutePath().normalize();

    private final List<ActiveSource<?>> activeSources;
    private final List<MonitorSource> monitorSources;
    private final List<PassiveSource> passiveSources;
    private final List<ConnectorSource> connectorSources;
    private final List<StreamGroup> streamGroups;
    private final List<PushSource> pushSources;

    private final Set<DataProducer<?>> sources;
    private final SchemaCatalogue schemaCatalogue;

    @SuppressWarnings("WeakerAccess")
    SourceCatalogue(
            SchemaCatalogue schemaCatalogue,
            List<ActiveSource<?>> activeSources,
            List<MonitorSource> monitorSources,
            List<PassiveSource> passiveSources,
            List<StreamGroup> streamGroups,
            List<ConnectorSource> connectorSources,
            List<PushSource> pushSources) {
        this.schemaCatalogue = schemaCatalogue;

        this.activeSources = activeSources;
        this.monitorSources = monitorSources;
        this.passiveSources = passiveSources;
        this.streamGroups = streamGroups;
        this.connectorSources = connectorSources;
        this.pushSources = pushSources;

        sources = new HashSet<>();

        sources.addAll(activeSources);
        sources.addAll(monitorSources);
        sources.addAll(passiveSources);
        sources.addAll(streamGroups);
        sources.addAll(connectorSources);
        sources.addAll(pushSources);
    }

    /**
     * Load the source catalogue based at the given root directory.
     * @param root Directory containing a specifications subdirectory.
     * @return parsed source catalogue.
     * @throws InvalidPathException if the {@code specifications} directory cannot be found in given
     *                              root.
     * @throws IOException if the source catalogue could not be read.
     */
    public static SourceCatalogue load(Path root) throws IOException {
        return load(root, new SourceConfig());
    }

    /**
     * Load the source catalogue based at the given root directory.
     * @param root Directory containing a specifications subdirectory.
     * @return parsed source catalogue.
     * @throws InvalidPathException if the {@code specifications} directory cannot be found in given
     *                              root.
     * @throws IOException if the source catalogue could not be read.
     */
    public static SourceCatalogue load(Path root, SourceConfig sourceConfig) throws IOException {
        Path specRoot = root.resolve("specifications");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // only serialize fields, not getters, etc.
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        SchemaCatalogue schemaCatalogue = new SchemaCatalogue(root);

        return new SourceCatalogue(
                schemaCatalogue,
                initSources(mapper.readerFor(ActiveSource.class), specRoot, Scope.ACTIVE, sourceConfig, sourceConfig.getActive()),
                initSources(mapper.readerFor(MonitorSource.class), specRoot, Scope.MONITOR, sourceConfig, sourceConfig.getMonitor()),
                initSources(mapper.readerFor(PassiveSource.class), specRoot, Scope.PASSIVE, sourceConfig, sourceConfig.getPassive()),
                initSources(mapper.readerFor(StreamGroup.class), specRoot, Scope.STREAM, sourceConfig, sourceConfig.getStream()),
                initSources(mapper.readerFor(ConnectorSource.class), specRoot, Scope.CONNECTOR, sourceConfig, sourceConfig.getConnector()),
                initSources(mapper.readerFor(PushSource.class), specRoot, Scope.PUSH, sourceConfig, sourceConfig.getPush()));
    }

    private static <T> List<T> initSources(ObjectReader reader, Path root, Scope scope,
            SourceConfig sourceConfig, List<T> otherSources) throws IOException {
        Path baseFolder = scope.getPath(root);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return otherSources;
        }

        FileSystem fs = FileSystems.getDefault();
        PathMatcher pathMatcher = sourceConfig.pathMatcher(fs);
        try (Stream<Path> walker = Files.walk(baseFolder)) {
            Stream<T> fileSources = walker
                    .filter(f -> Files.isRegularFile(f) && pathMatcher.matches(f))
                    .map(f -> {
                        try {
                            return reader.<T>readValue(f.toFile());
                        } catch (IOException ex) {
                            logger.error("Failed to load configuration {}: {}", f, ex.toString());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull);

            return Stream.concat(fileSources, otherSources.stream())
                    .collect(Collectors.toList());
        }
    }

    public SchemaCatalogue getSchemaCatalogue() {
        return schemaCatalogue;
    }

    /**
     * TODO.
     * @return TODO
     */
    public List<ActiveSource<?>> getActiveSources() {
        return activeSources;
    }

    /**
     * TODO.
     * @return TODO
     */
    public List<MonitorSource> getMonitorSources() {
        return monitorSources;
    }

    /**
     * TODO.
     * @return TODO
     */
    public List<PassiveSource> getPassiveSources() {
        return passiveSources;
    }

    public List<StreamGroup> getStreamGroups() {
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

    /**
     * TODO.
     * @return TODO
     */
    public List<ConnectorSource> getConnectorSources() {
        return connectorSources;
    }

    public List<PushSource> getPushSources() {
        return pushSources;
    }

    /** Get all topics in the catalogue. */
    public Stream<AvroTopic<?, ?>> getTopics() {
        return sources.stream()
                .flatMap(s -> s.getTopics(schemaCatalogue));
    }
}
