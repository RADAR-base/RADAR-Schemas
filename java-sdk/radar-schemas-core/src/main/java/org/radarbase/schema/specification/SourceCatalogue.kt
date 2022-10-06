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
package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.Scope
import org.radarbase.schema.specification.active.ActiveSource
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.specification.config.SourceConfig
import org.radarbase.schema.specification.connector.ConnectorSource
import org.radarbase.schema.specification.monitor.MonitorSource
import org.radarbase.schema.specification.passive.PassiveSource
import org.radarbase.schema.specification.push.PushSource
import org.radarbase.schema.specification.stream.StreamGroup
import org.radarbase.schema.validation.ValidationHelper
import org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH
import org.radarbase.topic.AvroTopic
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.*
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.exists
import kotlin.streams.asSequence

class SourceCatalogue internal constructor(
    val schemaCatalogue: SchemaCatalogue,
    val activeSources: List<ActiveSource<*>>,
    val monitorSources: List<MonitorSource>,
    val passiveSources: List<PassiveSource>,
    val streamGroups: List<StreamGroup>,
    val connectorSources: List<ConnectorSource>,
    val pushSources: List<PushSource>
) {

    val sources: Set<DataProducer<*>> = buildSet {
        addAll(activeSources)
        addAll(monitorSources)
        addAll(passiveSources)
        addAll(streamGroups)
        addAll(connectorSources)
        addAll(pushSources)
    }

    val topicNames: Stream<String>
        get() = sources.stream()
            .flatMap { it.topicNames }

    /** Get all topics in the catalogue.  */
    val topics: Stream<AvroTopic<*, *>>
        get() = sources.stream()
            .flatMap { it.getTopics(schemaCatalogue) }

    companion object {
        private val logger = LoggerFactory.getLogger(SourceCatalogue::class.java)

        /**
         * Load the source catalogue based at the given root directory.
         * @param root Directory containing a specifications subdirectory.
         * @return parsed source catalogue.
         * @throws InvalidPathException if the `specifications` directory cannot be found in given
         * root.
         * @throws IOException if the source catalogue could not be read.
         */
        @Throws(IOException::class, InvalidPathException::class)
        @JvmOverloads
        fun load(
            root: Path,
            schemaConfig: SchemaConfig = SchemaConfig(),
            sourceConfig: SourceConfig = SourceConfig(),
        ): SourceCatalogue {
            val specRoot = root.resolve(SPECIFICATIONS_PATH)
            val mapper = ObjectMapper(YAMLFactory()).apply {
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                setVisibility(
                    serializationConfig.defaultVisibilityChecker
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                )
            }
            val schemaCatalogue = SchemaCatalogue(
                root.resolve(ValidationHelper.COMMONS_PATH),
                schemaConfig,
            )
            val pathMatcher = sourceConfig.pathMatcher(specRoot)
            return SourceCatalogue(
                schemaCatalogue,
                initSources(mapper, specRoot, Scope.ACTIVE, pathMatcher, sourceConfig.active),
                initSources(mapper, specRoot, Scope.MONITOR, pathMatcher, sourceConfig.monitor),
                initSources(mapper, specRoot, Scope.PASSIVE, pathMatcher, sourceConfig.passive),
                initSources(mapper, specRoot, Scope.STREAM, pathMatcher, sourceConfig.stream),
                initSources(mapper, specRoot, Scope.CONNECTOR, pathMatcher, sourceConfig.connector),
                initSources(mapper, specRoot, Scope.PUSH, pathMatcher, sourceConfig.push)
            )
        }

        @Throws(IOException::class)
        private inline fun <reified T> initSources(
            mapper: ObjectMapper,
            root: Path,
            scope: Scope,
            sourceRootPathMatcher: PathMatcher,
            otherSources: List<T>,
        ): List<T> {
            val baseFolder = root.resolve(scope.lower)
            if (!baseFolder.exists()) {
                logger.info("{} sources folder not present at {}", scope, baseFolder)
                return otherSources
            }
            val reader = mapper.readerFor(T::class.java)
            return buildList {
                Files.walk(baseFolder).use { walker ->
                    walker
                        .asSequence()
                        .filter(sourceRootPathMatcher::matches)
                        .forEach { p ->
                            try {
                                add(reader.readValue(p.toFile()))
                            } catch (ex: IOException) {
                                logger.error("Failed to load configuration {}: {}", p, ex.toString())
                            }
                        }
                }
                addAll(otherSources)
            }
        }
    }
}
