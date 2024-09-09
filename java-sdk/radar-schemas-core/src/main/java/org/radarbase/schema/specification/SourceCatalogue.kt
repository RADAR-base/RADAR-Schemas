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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.radarbase.kotlin.coroutines.forkJoin
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
import org.radarbase.schema.util.SchemaUtils.listRecursive
import org.radarbase.schema.validation.ValidationHelper
import org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH
import org.radarbase.topic.AvroTopic
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.stream.Stream
import kotlin.io.path.exists

class SourceCatalogue internal constructor(
    val schemaCatalogue: SchemaCatalogue,
    val activeSources: List<ActiveSource<*>>,
    val monitorSources: List<MonitorSource>,
    val passiveSources: List<PassiveSource>,
    val streamGroups: List<StreamGroup>,
    val connectorSources: List<ConnectorSource>,
    val pushSources: List<PushSource>,
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
            .flatMap { it.topics(schemaCatalogue) }
}

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
suspend fun SourceCatalogue(
    root: Path,
    schemaConfig: SchemaConfig,
    sourceConfig: SourceConfig,
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
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE),
        )
    }
    val pathMatcher = sourceConfig.pathMatcher(specRoot)

    return coroutineScope {
        val schemaCatalogueJob = async {
            SchemaCatalogue(
                root.resolve(ValidationHelper.COMMONS_PATH),
                schemaConfig,
            )
        }
        val activeJob = async {
            initSources(mapper, specRoot, Scope.ACTIVE, pathMatcher, sourceConfig.active)
        }
        val monitorJob = async {
            initSources(mapper, specRoot, Scope.MONITOR, pathMatcher, sourceConfig.monitor)
        }
        val passiveJob = async {
            initSources(mapper, specRoot, Scope.PASSIVE, pathMatcher, sourceConfig.passive)
        }
        val streamJob = async {
            initSources(mapper, specRoot, Scope.STREAM, pathMatcher, sourceConfig.stream)
        }
        val connectorJob = async {
            initSources(
                mapper,
                specRoot,
                Scope.CONNECTOR,
                pathMatcher,
                sourceConfig.connector,
            )
        }
        val pushJob = async {
            initSources(mapper, specRoot, Scope.PUSH, pathMatcher, sourceConfig.push)
        }

        SourceCatalogue(
            schemaCatalogueJob.await(),
            activeSources = activeJob.await(),
            monitorSources = monitorJob.await(),
            passiveSources = passiveJob.await(),
            streamGroups = streamJob.await(),
            connectorSources = connectorJob.await(),
            pushSources = pushJob.await(),
        )
    }
}

@Throws(IOException::class)
private suspend inline fun <reified T> initSources(
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
    val fileList = baseFolder.listRecursive(sourceRootPathMatcher::matches)
    return buildList(fileList.size + otherSources.size) {
        fileList
            .forkJoin<Path, T?>(Dispatchers.IO) { p ->
                try {
                    reader.readValue<T>(p.toFile())
                } catch (ex: IOException) {
                    logger.error("Failed to load configuration {}: {}", p, ex.toString())
                    null
                }
            }
            .filterIsInstanceTo(this@buildList)
        addAll(otherSources)
    }
}
