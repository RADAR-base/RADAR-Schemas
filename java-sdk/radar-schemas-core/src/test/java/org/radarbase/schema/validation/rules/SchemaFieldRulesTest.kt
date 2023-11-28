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

import kotlinx.coroutines.runBlocking
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.SchemaBuilder.FieldAssembler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.validation.ValidationHelper.toRecordName
import org.radarbase.schema.validation.toFormattedString
import org.radarbase.schema.validation.validate
import java.nio.file.Paths

class SchemaFieldRulesTest {
    private lateinit var validator: SchemaFieldRules
    private lateinit var schemaValidator: SchemaRules

    @BeforeEach
    fun setUp() {
        validator = SchemaFieldRules()
        schemaValidator = SchemaRules(validator)
    }

    @Test
    fun fileNameTest() {
        assertEquals(
            "Questionnaire",
            Paths.get("/path/to/questionnaire.avsc").toRecordName(),
        )
        assertEquals(
            "ApplicationExternalTime",
            Paths.get("/path/to/application_external_time.avsc").toRecordName(),
        )
    }

    @Test
    fun fieldNameRegex() {
        assertTrue("interBeatInterval".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("x".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue(SchemaRules.TIME.matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("subjectId".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("listOfSeveralThings".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertFalse("Time".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
        assertFalse("E4Heart".matches(SchemaFieldRules.FIELD_NAME_PATTERN))
    }

    @Test
    fun fieldsTest() = runBlocking {
        assertFieldsErrorCount(1, validator.isFieldTypeValid, "Should have at least one field")

        assertFieldsErrorCount(0, validator.isFieldTypeValid, "Single optional field should be fine") {
            optionalBoolean("optional")
        }
    }

    private suspend fun assertFieldsErrorCount(
        count: Int,
        fieldValidator: Validator<SchemaField>,
        message: String,
        schemaBuilder: FieldAssembler<Schema>.() -> Unit = {},
    ) {
        val result = schemaValidator.isFieldsValid(fieldValidator)
            .validate(
                SchemaBuilder.builder("org.radarcns.monitor.test")
                    .record("RecordName")
                    .fields()
                    .apply(schemaBuilder)
                    .endRecord(),
            )
        assertEquals(count, result.size) { message + result.toFormattedString() }
    }

    @Test
    fun fieldNameTest() = runBlocking {
        assertFieldsErrorCount(1, validator.isNameValid, "Field names should not start with uppercase") {
            requiredString("Field1")
        }
        assertFieldsErrorCount(0, validator.isNameValid, "Field name timeReceived is correct") {
            requiredDouble("timeReceived")
        }
    }

    @Test
    fun fieldDocumentationTest() = runBlocking {
        assertFieldsErrorCount(2, validator.isDocumentationValid, "Documentation should be reported missing or incorrectly formatted.") {
            name("userId").doc("Documentation").type("string").noDefault()
            name("sourceId").type("string").noDefault()
        }
        assertFieldsErrorCount(0, validator.isDocumentationValid, "Documentation should be valid") {
            name("userId").doc("Documentation.").type("string").noDefault()
        }
    }

    @Test
    fun defaultValueExceptionTest() = runBlocking {
        assertFieldsErrorCount(1, validator.isDefaultValueValid, "Enum fields should have a default.") {
            name("Field1")
                .type(
                    SchemaBuilder.enumeration("org.radarcns.test.EnumeratorTest")
                        .symbols("VAL", "UNKNOWN"),
                )
                .noDefault()
        }
    }

    @Test // TODO improve test after having define the default guideline
    fun defaultValueTest() = runBlocking {
        val serverStatusEnum = SchemaBuilder.enumeration("org.radarcns.monitor.test.ServerStatus")
            .symbols("Connected", "NotConnected", "UNKNOWN")

        assertFieldsErrorCount(0, validator.isDefaultValueValid, "Enum fields should have an UNKNOWN default.") {
            name("serverStatus").type(serverStatusEnum).withDefault("UNKNOWN")
        }
        assertFieldsErrorCount(1, validator.isDefaultValueValid, "Enum fields with no UNKNOWN default should be reported.") {
            name("serverStatus").type(serverStatusEnum).noDefault()
        }
    }
}
