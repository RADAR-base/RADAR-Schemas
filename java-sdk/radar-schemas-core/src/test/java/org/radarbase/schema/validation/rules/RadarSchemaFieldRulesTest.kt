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
import org.apache.avro.Schema.Parser
import org.apache.avro.SchemaBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.validation.ValidationHelper.getRecordName
import org.radarbase.schema.validation.validate
import java.nio.file.Paths

/**
 * TODO.
 */
class RadarSchemaFieldRulesTest {
    private lateinit var validator: RadarSchemaFieldRules
    private lateinit var schemaValidator: RadarSchemaRules

    @BeforeEach
    fun setUp() {
        validator = RadarSchemaFieldRules()
        schemaValidator = RadarSchemaRules(validator)
    }

    @Test
    fun fileNameTest() {
        Assertions.assertEquals(
            "Questionnaire",
            getRecordName(Paths.get("/path/to/questionnaire.avsc")),
        )
        Assertions.assertEquals(
            "ApplicationExternalTime",
            getRecordName(
                Paths.get("/path/to/application_external_time.avsc"),
            ),
        )
    }

    @Test
    fun fieldNameRegex() {
        assertTrue("interBeatInterval".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("x".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue(RadarSchemaRules.TIME.matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("subjectId".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertTrue("listOfSeveralThings".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertFalse("Time".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
        assertFalse("E4Heart".matches(RadarSchemaFieldRules.FIELD_NAME_PATTERN))
    }

    @Test
    fun fieldsTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        var result = schemaValidator.isFieldsValid(validator.validateFieldTypes(schemaValidator))
            .validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .optionalBoolean("optional")
            .endRecord()
        result = schemaValidator.isFieldsValid(validator.validateFieldTypes(schemaValidator))
            .validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun fieldNameTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredString(FIELD_NUMBER_MOCK)
            .endRecord()
        var result = schemaValidator.isFieldsValid(validator.isNameValid).validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredDouble("timeReceived")
            .endRecord()
        result = schemaValidator.isFieldsValid(validator.isNameValid).validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun fieldDocumentationTest() = runBlocking {
        var schema: Schema = Parser().parse(
            """{
                |"namespace": "org.radarcns.kafka.key",
                |"type": "record",
                |"name": "key", "type":
                |"record",
                |"fields": [
                |{"name": "userId", "type": "string" , "doc": "Documentation"},
                |{"name": "sourceId", "type": "string"} ]
                |}
            """.trimMargin(),
        )
        var result = schemaValidator.isFieldsValid(validator.isDocumentationValid).validate(schema)
        Assertions.assertEquals(2, result.count())
        schema = Parser().parse(
            """{
                |"namespace": "org.radarcns.kafka.key",
                |"type": "record",
                |"name": "key",
                |"type": "record",
                |"fields": [
                |{"name": "userId", "type": "string" , "doc": "Documentation."}]
                |}
            """.trimMargin(),
        )
        result = schemaValidator.isFieldsValid(validator.isDocumentationValid).validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun defaultValueExceptionTest() = runBlocking {
        val result = schemaValidator.isFieldsValid(
            validator.isDefaultValueValid,
        )
            .validate(
                SchemaBuilder.record(RECORD_NAME_MOCK)
                    .fields()
                    .name(FIELD_NUMBER_MOCK)
                    .type(
                        SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
                            .symbols("VAL", UNKNOWN_MOCK),
                    )
                    .noDefault()
                    .endRecord(),
            )
        Assertions.assertEquals(1, result.count())
    }

    @Test // TODO improve test after having define the default guideline
    fun defaultValueTest() = runBlocking {
        val schemaTxtInit = (
            "{\"namespace\": \"org.radarcns.test\", " +
                "\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": "
            )
        var schema: Schema = Parser().parse(
            schemaTxtInit +
                "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": " +
                "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, " +
                "\"default\": \"UNKNOWN\" } ] }",
        )
        var result =
            schemaValidator.isFieldsValid(validator.isDefaultValueValid).validate(schema)
        Assertions.assertEquals(0, result.count())
        schema = Parser().parse(
            schemaTxtInit +
                "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": " +
                "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, " +
                "\"default\": \"null\" } ] }",
        )
        result = schemaValidator.isFieldsValid(validator.isDefaultValueValid).validate(schema)
        Assertions.assertEquals(1, result.count())
    }

    companion object {
        private const val MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test"
        private const val ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest"
        private const val UNKNOWN_MOCK = "UNKNOWN"
        private const val RECORD_NAME_MOCK = "RecordName"
        private const val FIELD_NUMBER_MOCK = "Field1"
    }
}
