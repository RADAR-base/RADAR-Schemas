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
import org.radarbase.schema.validation.validate

class RadarSchemaRulesTest {
    private lateinit var validator: SchemaRules

    @BeforeEach
    fun setUp() {
        validator = SchemaRules()
    }

    @Test
    fun nameSpaceRegex() {
        assertTrue("org.radarcns".matches(SchemaRules.NAMESPACE_PATTERN))
        assertFalse("Org.radarcns".matches(SchemaRules.NAMESPACE_PATTERN))
        assertFalse("org.radarCns".matches(SchemaRules.NAMESPACE_PATTERN))
        assertFalse(".org.radarcns".matches(SchemaRules.NAMESPACE_PATTERN))
        assertFalse("org.radar-cns".matches(SchemaRules.NAMESPACE_PATTERN))
        assertFalse("org.radarcns.empaticaE4".matches(SchemaRules.NAMESPACE_PATTERN))
    }

    @Test
    fun recordNameRegex() {
        assertTrue("Questionnaire".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("EmpaticaE4Acceleration".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("Heart4Me".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("Heart4M".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("Heart4".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("Heart4me".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("Heart4ME".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("4Me".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("TTest".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("questionnaire".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("questionnaire4".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("questionnaire4Me".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertFalse("questionnaire4me".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("A4MM".matches(SchemaRules.RECORD_NAME_PATTERN))
        assertTrue("Aaaa4MMaa".matches(SchemaRules.RECORD_NAME_PATTERN))
    }

    @Test
    fun enumerationRegex() {
        assertTrue("PHQ8".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertTrue("HELLO".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertTrue("HELLOTHERE".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertTrue("HELLO_THERE".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertFalse("Hello".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertFalse("hello".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertFalse("HelloThere".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertFalse("Hello_There".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
        assertFalse("HELLO.THERE".matches(SchemaRules.ENUM_SYMBOL_PATTERN))
    }

    @Test
    fun nameSpaceTest() = runBlocking {
        val schema = SchemaBuilder
            .builder("org.radarcns.active.questionnaire")
            .record("Questionnaire")
            .fields()
            .endRecord()
        val result = validator.isNamespaceValid.validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun nameSpaceInvalidDashTest() = runBlocking {
        val schema = SchemaBuilder
            .builder("org.radar-cns.monitors.test")
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        val result = validator.isNamespaceValid
            .validate(schema)
        Assertions.assertEquals(1, result.count())
    }

    @Test
    fun recordNameTest() = runBlocking {
        val schema = SchemaBuilder
            .builder("org.radarcns.active.testactive")
            .record("Schema")
            .fields()
            .endRecord()
        val result = validator.isNameValid.validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun fieldsTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        var result = validator.isFieldsValid(
            validator.fieldRules.isFieldTypeValid,
        ).validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .optionalBoolean("optional")
            .endRecord()
        result = validator.isFieldsValid(validator.fieldRules.isFieldTypeValid)
            .validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun timeTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder("org.radarcns.time.test")
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredString("string")
            .endRecord()
        var result = validator.hasTime.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .builder("org.radarcns.time.test")
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredDouble(SchemaRules.TIME)
            .endRecord()
        result = validator.hasTime.validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun timeCompletedTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(ACTIVE_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredString("field")
            .endRecord()
        var result = validator.hasTimeCompleted.validate(schema)
        Assertions.assertEquals(1, result.count())
        result = validator.hasNoTimeCompleted.validate(schema)
        Assertions.assertEquals(0, result.count())
        schema = SchemaBuilder
            .builder(ACTIVE_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredDouble("timeCompleted")
            .endRecord()
        result = validator.hasTimeCompleted.validate(schema)
        Assertions.assertEquals(0, result.count())
        result = validator.hasNoTimeCompleted.validate(schema)
        Assertions.assertEquals(1, result.count())
    }

    @Test
    fun timeReceivedTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredString("field")
            .endRecord()
        var result = validator.hasTimeReceived.validate(schema)
        Assertions.assertEquals(1, result.count())
        result = validator.hasNoTimeReceived.validate(schema)
        Assertions.assertEquals(0, result.count())
        schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .requiredDouble("timeReceived")
            .endRecord()
        result = validator.hasTimeReceived.validate(schema)
        Assertions.assertEquals(0, result.count())
        result = validator.hasNoTimeReceived.validate(schema)
        Assertions.assertEquals(1, result.count())
    }

    @Test
    fun schemaDocumentationTest() = runBlocking {
        var schema: Schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .fields()
            .endRecord()
        var result = validator.isDocumentationValid.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .builder(MONITOR_NAME_SPACE_MOCK)
            .record(RECORD_NAME_MOCK)
            .doc("Documentation.")
            .fields()
            .endRecord()
        result = validator.isDocumentationValid.validate(schema)
        Assertions.assertEquals(0, result.count())
    }

    @Test
    fun enumerationSymbolsTest() = runBlocking {
        var schema: Schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("TEST", UNKNOWN_MOCK)
        var result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(0, result.count())
        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK).symbols()
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(1, result.count())
    }

    @Test
    fun enumerationSymbolTest() = runBlocking {
        val enumName = "org.radarcns.monitor.application.ApplicationServerStatus"
        val connected = "CONNECTED"
        var schema: Schema = SchemaBuilder
            .enumeration(enumName)
            .symbols(connected, "DISCONNECTED", UNKNOWN_MOCK)
        var result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(0, result.count())
        val schemaTxtInit = (
            "{\"namespace\": \"org.radarcns.monitor.application\", " +
                "\"name\": \"ServerStatus\", \"type\": " +
                "\"enum\", \"symbols\": ["
            )
        val schemaTxtEnd = "] }"
        schema = Parser().parse(
            schemaTxtInit +
                "\"CONNECTED\", \"NOT_CONNECTED\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd,
        )
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(0, result.count())
        schema = SchemaBuilder
            .enumeration(enumName)
            .symbols(connected, "disconnected", UNKNOWN_MOCK)
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .enumeration(enumName)
            .symbols(connected, "Not_Connected", UNKNOWN_MOCK)
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = SchemaBuilder
            .enumeration(enumName)
            .symbols(connected, "NotConnected", UNKNOWN_MOCK)
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = Parser().parse(
            schemaTxtInit +
                "\"CONNECTED\", \"Not_Connected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd,
        )
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(1, result.count())
        schema = Parser().parse(
            schemaTxtInit +
                "\"Connected\", \"NotConnected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd,
        )
        result = validator.isEnumSymbolsValid.validate(schema)
        Assertions.assertEquals(2, result.count())
    }

    @Test
    fun testUniqueness() = runBlocking {
        val prefix = (
            "{\"namespace\": \"org.radarcns.monitor.application\", " +
                "\"name\": \""
            )
        val infix = "\", \"type\": \"enum\", \"symbols\": "
        val suffix = '}'
        val schema = Parser().parse(
            prefix + "ServerStatus" +
                infix + "[\"A\", \"B\"]" + suffix,
        )
        var result = validator.isUnique.validate(schema)
        Assertions.assertEquals(0, result.count())
        result = validator.isUnique.validate(schema)
        Assertions.assertEquals(0, result.count())
        val schemaAlt = Parser().parse(
            prefix + "ServerStatus" +
                infix + "[\"A\", \"B\", \"C\"]" + suffix,
        )
        result = validator.isUnique.validate(schemaAlt)
        Assertions.assertEquals(1, result.count())
        result = validator.isUnique.validate(schemaAlt)
        Assertions.assertEquals(1, result.count())
        val schema2 = Parser().parse(
            prefix + "ServerStatus2" +
                infix + "[\"A\", \"B\"]" + suffix,
        )
        result = validator.isUnique.validate(schema2)
        Assertions.assertEquals(0, result.count())
        val schema3 = Parser().parse(
            prefix + "ServerStatus" +
                infix + "[\"A\", \"B\"]" + suffix,
        )
        result = validator.isUnique.validate(schema3)
        Assertions.assertEquals(0, result.count())
        result = validator.isUnique.validate(schema3)
        Assertions.assertEquals(0, result.count())
        result = validator.isUnique.validate(schemaAlt)
        Assertions.assertEquals(1, result.count())
    }

    companion object {
        private const val ACTIVE_NAME_SPACE_MOCK = "org.radarcns.active.test"
        private const val MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test"
        private const val ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest"
        private const val UNKNOWN_MOCK = "UNKNOWN"
        private const val RECORD_NAME_MOCK = "RecordName"
    }
}
