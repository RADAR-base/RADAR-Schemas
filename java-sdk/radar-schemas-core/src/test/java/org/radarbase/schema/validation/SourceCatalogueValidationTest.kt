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
package org.radarbase.schema.validation

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.opentest4j.MultipleFailuresError
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.DataTopic
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.specification.config.SourceConfig
import org.radarbase.schema.validation.ValidationHelper.isValidTopic
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.Stream

class SourceCatalogueValidationTest {
    @Test
    fun validateTopicNames() {
        catalogue.topicNames.forEach { topic: String ->
            assertTrue(isValidTopic(topic), "$topic is invalid")
        }
    }

    @Test
    fun validateTopics() {
        val expected = Stream.of<List<DataProducer<out DataTopic>>>(
            catalogue.activeSources,
            catalogue.monitorSources,
            catalogue.passiveSources,
            catalogue.streamGroups,
            catalogue.connectorSources,
            catalogue.pushSources,
        )
            .flatMap { it.stream() }
            .flatMap(DataProducer<*>::topicNames)
            .sorted()
            .collect(Collectors.toList())
        Assertions.assertEquals(
            expected,
            catalogue.topicNames.sorted().collect(Collectors.toList()),
        )
    }

    @Test
    fun validateTopicSchemas() {
        catalogue.sources.stream()
            .flatMap { source: DataProducer<*> -> source.data.stream() }
            .forEach { data ->
                try {
                    assertTrue(
                        data.topics(catalogue.schemaCatalogue)
                            .findAny()
                            .isPresent,
                    )
                } catch (ex: IOException) {
                    fail("Cannot create topic from specification: $ex")
                }
            }
    }

    @Test
    fun validateSerialization() {
        val mapper = ObjectMapper()
        val failures = catalogue.sources
            .stream()
            .map { source: DataProducer<*> ->
                try {
                    val json = mapper.writeValueAsString(source)
                    assertFalse(json.contains("\"parallel\":false"))
                    return@map null
                } catch (ex: Exception) {
                    return@map IllegalArgumentException("Source $source is not valid", ex)
                }
            }
            .filter(Objects::nonNull)
            .collect(Collectors.toList())

        if (failures.isNotEmpty()) {
            throw MultipleFailuresError("One or more sources were not valid", failures)
        }
    }

    companion object {
        private lateinit var catalogue: SourceCatalogue

        val BASE_PATH: Path = Paths.get("../..").toAbsolutePath().normalize()

        @BeforeAll
        @JvmStatic
        @Throws(IOException::class)
        fun setUp() {
            catalogue = runBlocking {
                SourceCatalogue(BASE_PATH, SchemaConfig(), SourceConfig())
            }
        }
    }
}
