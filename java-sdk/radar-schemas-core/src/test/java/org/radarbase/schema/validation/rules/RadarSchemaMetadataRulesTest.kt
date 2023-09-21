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
package org.radarbase.schema.validation.rules

import org.apache.avro.SchemaBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.Scope.MONITOR
import org.radarbase.schema.Scope.PASSIVE
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.SchemaValidator.Companion.format
import org.radarbase.schema.validation.SourceCatalogueValidationTest
import org.radarbase.schema.validation.ValidationException
import org.radarbase.schema.validation.ValidationHelper
import java.nio.file.Paths
import java.util.stream.Stream

/**
 * TODO.
 */
class RadarSchemaMetadataRulesTest {
    private lateinit var validator: RadarSchemaMetadataRules

    @BeforeEach
    fun setUp() {
        val config = SchemaConfig()
        validator = RadarSchemaMetadataRules(
            SourceCatalogueValidationTest.BASE_PATH.resolve(ValidationHelper.COMMONS_PATH), config,
        )
    }

    @Test
    fun fileNameTest() {
        assertEquals(
            "Questionnaire",
            ValidationHelper.getRecordName(Paths.get("/path/to/questionnaire.avsc")),
        )
        assertEquals(
            "ApplicationExternalTime",
            ValidationHelper.getRecordName(
                Paths.get("/path/to/application_external_time.avsc"),
            ),
        )
    }

    @Test
    fun nameSpaceInvalidPlural() {
        val schema = SchemaBuilder
            .builder("org.radarcns.monitors.test")
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        val root =
            MONITOR.getPath(SourceCatalogueValidationTest.BASE_PATH.resolve(ValidationHelper.COMMONS_PATH))
        assertNotNull(root)
        val path = root.resolve("test/record_name.avsc")
        val result = validator.validateSchemaLocation()
            .validate(SchemaMetadata(schema, MONITOR, path))
        assertEquals(1, result.count())
    }

    @Test
    fun nameSpaceInvalidLastPartPlural() {
        val schema = SchemaBuilder
            .builder("org.radarcns.monitor.tests")
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        val root =
            MONITOR.getPath(SourceCatalogueValidationTest.BASE_PATH.resolve(ValidationHelper.COMMONS_PATH))
        assertNotNull(root)
        val path = root.resolve("test/record_name.avsc")
        val result = validator.validateSchemaLocation()
            .validate(SchemaMetadata(schema, MONITOR, path))
        assertEquals(1, result.count())
    }

    @Test
    fun recordNameTest() {
        // misspell aceleration
        var fieldName = "EmpaticaE4Aceleration"
        var filePath = Paths.get("/path/to/empatica_e4_acceleration.avsc")
        var schema = SchemaBuilder
            .builder("org.radarcns.passive.empatica")
            .record(fieldName)
            .fields()
            .endRecord()
        var result: Stream<ValidationException> = validator.validateSchemaLocation()
            .validate(SchemaMetadata(schema, PASSIVE, filePath))
        assertEquals(2, result.count())
        fieldName = "EmpaticaE4Acceleration"
        filePath =
            SourceCatalogueValidationTest.BASE_PATH.resolve("commons/passive/empatica/empatica_e4_acceleration.avsc")
        schema = SchemaBuilder
            .builder("org.radarcns.passive.empatica")
            .record(fieldName)
            .fields()
            .endRecord()
        result = validator.validateSchemaLocation()
            .validate(SchemaMetadata(schema, PASSIVE, filePath))
        assertEquals("", format(result))
    }

    companion object {
        private const val RECORD_NAME_MOCK = "RecordName"
    }
}
