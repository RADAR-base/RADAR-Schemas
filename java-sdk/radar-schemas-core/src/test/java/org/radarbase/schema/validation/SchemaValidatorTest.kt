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

import kotlinx.coroutines.runBlocking
import org.apache.avro.SchemaBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.ACTIVE
import org.radarbase.schema.Scope.CATALOGUE
import org.radarbase.schema.Scope.CONNECTOR
import org.radarbase.schema.Scope.KAFKA
import org.radarbase.schema.Scope.MONITOR
import org.radarbase.schema.Scope.PASSIVE
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.specification.config.SourceConfig
import org.radarbase.schema.validation.ValidationHelper.COMMONS_PATH
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

class SchemaValidatorTest {
    private lateinit var validator: SchemaValidator

    @BeforeEach
    fun setUp() {
        val config = SchemaConfig()
        validator = SchemaValidator(COMMONS_ROOT, config)
    }

    @Test
    @Throws(IOException::class)
    fun active() {
        testScope(ACTIVE)
    }

    @Test
    @Throws(IOException::class)
    fun activeSpecifications() {
        testFromSpecification(ACTIVE)
    }

    @Test
    @Throws(IOException::class)
    fun monitor() {
        testScope(MONITOR)
    }

    @Test
    @Throws(IOException::class)
    fun monitorSpecifications() {
        testFromSpecification(MONITOR)
    }

    @Test
    @Throws(IOException::class)
    fun passive() {
        testScope(PASSIVE)
    }

    @Test
    @Throws(IOException::class)
    fun passiveSpecifications() {
        testFromSpecification(PASSIVE)
    }

    @Test
    @Throws(IOException::class)
    fun kafka() {
        testScope(KAFKA)
    }

    @Test
    @Throws(IOException::class)
    fun kafkaSpecifications() {
        testFromSpecification(KAFKA)
    }

    @Test
    @Throws(IOException::class)
    fun catalogue() {
        testScope(CATALOGUE)
    }

    @Test
    @Throws(IOException::class)
    fun catalogueSpecifications() {
        testFromSpecification(CATALOGUE)
    }

    @Test
    @Throws(IOException::class)
    fun connectorSchemas() {
        testScope(CONNECTOR)
    }

    @Test
    @Throws(IOException::class)
    fun connectorSpecifications() {
        testFromSpecification(CONNECTOR)
    }

    @Throws(IOException::class)
    private fun testFromSpecification(scope: Scope) = runBlocking {
        val sourceCatalogue = SourceCatalogue(ROOT, SchemaConfig(), SourceConfig())
        val result = validator.analyseSourceCatalogue(scope, sourceCatalogue).toFormattedString()
        if (result.isNotEmpty()) {
            fail<Any>(result)
        }
    }

    @Throws(IOException::class)
    private fun testScope(scope: Scope) = runBlocking {
        val schemaCatalogue = SchemaCatalogue(
            COMMONS_ROOT,
            SchemaConfig(),
            scope,
        )
        val result = validator.analyseFiles(schemaCatalogue, scope).toFormattedString()
        if (result.isNotEmpty()) {
            fail<Any>(result)
        }
    }

    @Test
    fun testEnumerator() = runBlocking {
        val schemaPath = COMMONS_ROOT.resolve(
            "monitor/application/application_server_status.avsc",
        )
        val name = "org.radarcns.monitor.application.ApplicationServerStatus"
        val documentation = "Mock documentation."
        var schema = SchemaBuilder
            .enumeration(name)
            .doc(documentation)
            .symbols("CONNECTED", "DISCONNECTED", "UNKNOWN")
        var result = validationContext {
            with(validator) {
                validate(schema, schemaPath, MONITOR)
            }
        }
        assertEquals(0, result.count())
        schema = SchemaBuilder
            .enumeration(name)
            .doc(documentation)
            .symbols("CONNECTED", "DISCONNECTED", "un_known")
        result = validationContext {
            with(validator) {
                validate(schema, schemaPath, MONITOR)
            }
        }
        assertEquals(2, result.count())
    }

    companion object {
        private val ROOT = Paths.get("../..").toAbsolutePath().normalize()
        private val COMMONS_ROOT: Path = ROOT.resolve(COMMONS_PATH)
    }
}
