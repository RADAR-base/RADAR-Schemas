package org.radarcns.validator.util.unit;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.validator.AvroValidator.FIELD_NAME_REGEX;
import static org.radarcns.validator.StructureValidator.NameFolder.ACTIVE;
import static org.radarcns.validator.StructureValidator.NameFolder.MONITOR;
import static org.radarcns.validator.util.SchemaValidator.ENUMERATION_SYMBOL_REGEX;
import static org.radarcns.validator.util.SchemaValidator.NAMESPACE_REGEX;
import static org.radarcns.validator.util.SchemaValidator.RECORD_NAME_REGEX;

import java.util.Collections;
import java.util.Optional;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.radarcns.validator.util.SchemaValidator;
import org.radarcns.validator.util.ValidationResult;
import org.radarcns.validator.util.ValidationSupport;

public class SchemaValidatorTest {

    private static final String ACTIVE_NAME_SPACE_MOCK = "org.radarcns.active.test";
    private static final String MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test";

    private static final String RECORD_NAME_MOCK = "RecordName";
    private static final String FIELD_NUMBER_MOCK = "Field1";

    private static final String INVALID_TEXT = " is invalid.";

    @Test
    public void fileNameTest() {
        assertEquals("Questionnaire",
                ValidationSupport.getRecordName("questionnaire.avsc"));
        assertEquals("ApplicationExternalTime",
                ValidationSupport.getRecordName("application_external_time.avsc"));
    }

    @Test
    public void nameSpaceRegex() {
        assertTrue("org.radarcns".matches(NAMESPACE_REGEX));
        assertFalse("Org.radarcns".matches(NAMESPACE_REGEX));
        assertFalse("org.radarCns".matches(NAMESPACE_REGEX));
        assertFalse(".org.radarcns".matches(NAMESPACE_REGEX));
        assertFalse("org.radar-cns".matches(NAMESPACE_REGEX));
        assertFalse("org.radarcns.empaticaE4".matches(NAMESPACE_REGEX));
    }

    @Test
    public void recordNameRegex() {
        assertTrue("Questionnaire".matches(RECORD_NAME_REGEX));
        assertTrue("EmpaticaE4Acceleration".matches(RECORD_NAME_REGEX));
        assertTrue("Heart4Me".matches(RECORD_NAME_REGEX));
        assertTrue("Heart4M".matches(RECORD_NAME_REGEX));

        assertFalse("Heart4".matches(RECORD_NAME_REGEX));
        assertFalse("Heart4me".matches(RECORD_NAME_REGEX));
        assertFalse("Heart4ME".matches(RECORD_NAME_REGEX));
        assertFalse("4Me".matches(RECORD_NAME_REGEX));
        assertFalse("TTest".matches(RECORD_NAME_REGEX));
        assertFalse("questionnaire".matches(RECORD_NAME_REGEX));
        assertFalse("questionnaire4".matches(RECORD_NAME_REGEX));
        assertFalse("questionnaire4Me".matches(RECORD_NAME_REGEX));
        assertFalse("questionnaire4me".matches(RECORD_NAME_REGEX));
        assertFalse("A4MM".matches(RECORD_NAME_REGEX));
        assertFalse("Aaaa4MMaa".matches(RECORD_NAME_REGEX));
    }

    @Test
    public void fieldNameRegex() {
        assertTrue("x".matches(FIELD_NAME_REGEX));
        assertTrue(SchemaValidator.TIME.matches(FIELD_NAME_REGEX));
        assertTrue("subjectId".matches(FIELD_NAME_REGEX));
        assertTrue("listOfSeveralThings".matches(FIELD_NAME_REGEX));
        assertFalse("Time".matches(FIELD_NAME_REGEX));
        assertFalse("E4Heart".matches(FIELD_NAME_REGEX));
    }

    @Test
    public void enumerationRegex() {
        assertTrue("HELLO".matches(ENUMERATION_SYMBOL_REGEX));
        assertTrue("HELLOTHERE".matches(ENUMERATION_SYMBOL_REGEX));
        assertTrue("HELLO_THERE".matches(ENUMERATION_SYMBOL_REGEX));
        assertFalse("Hello".matches(ENUMERATION_SYMBOL_REGEX));
        assertFalse("hello".matches(ENUMERATION_SYMBOL_REGEX));
        assertFalse("HelloThere".matches(ENUMERATION_SYMBOL_REGEX));
        assertFalse("Hello_There".matches(ENUMERATION_SYMBOL_REGEX));
        assertFalse("HELLO.THERE".matches(ENUMERATION_SYMBOL_REGEX));
    }

    @Test
    public void nameSpaceTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.active.questionnaire")
                    .record("Questionnaire")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(ACTIVE,
                "questionnaire").apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder("org.radar-cns.monitors.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(MONITOR, "test").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Namespace cannot be null and must fully lowercase dot "
                + "separated without numeric. In this case the expected value is "
                + "\"org.radarcns.monitor.test\". org.radar-cns.monitors.test."
                + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.monitors.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(MONITOR, "test").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Namespace cannot be null and must fully lowercase dot "
                + "separated without numeric. In this case the expected value is "
                + "\"org.radarcns.monitor.test\". org.radarcns.monitors.test."
                + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.tests")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(MONITOR, "test").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Namespace cannot be null and must fully lowercase dot "
                + "separated without numeric. In this case the expected value is "
                + "\"org.radarcns.monitor.test\". org.radarcns.monitor.tests."
                + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());
    }

    @Test
    public void recordNameTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.active.testactive")
                    .record("Schema")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateRecordName("schema.avsc").apply(schema);

        assertTrue(result.isValid());

        String fieldName = "EmpaticaE4Aceleration";
        String fileName = "empatica_e4_acceleration.avsc";

        schema = SchemaBuilder
                    .builder("org.radarcns.passive.empatica")
                    .record(fieldName)
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateRecordName(fileName).apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Record name must be the conversion of the .avsc file name in "
                + "UpperCamelCase and name the device explicitly. The expected value is "
                + "EmpaticaE4Acceleration\". org.radarcns.passive.empatica."
                + fieldName + " is invalid."),
                result.getReason());

        result = SchemaValidator.validateRecordName(
                fileName, Collections.singleton(fieldName)).apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void fieldsTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        result = SchemaValidator.validateFields().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Avro Record must have field list. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)), result.getReason());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = SchemaValidator.validateFields().apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void timeTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("string")
                    .endRecord();

        result = SchemaValidator.validateTime().apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Any schema representing collected data must have a \"time\" field"
                + " formatted in DOUBLE. org.radarcns.time.test." + RECORD_NAME_MOCK
                + INVALID_TEXT),
                result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble(SchemaValidator.TIME)
                    .endRecord();

        result = SchemaValidator.validateTime().apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void timeCompletedTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder(ACTIVE_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidator.validateTimeCompleted().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any ACTIVE schema must have a \"timeCompleted\" field formatted "
                + "in DOUBLE. org.radarcns.active.test." + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        result = SchemaValidator.validateNotTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                      .builder(ACTIVE_NAME_SPACE_MOCK)
                      .record(RECORD_NAME_MOCK)
                      .fields()
                      .requiredDouble("timeCompleted")
                      .endRecord();

        result = SchemaValidator.validateTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidator.validateNotTimeCompleted().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("\"timeCompleted\" is allow only in ACTIVE schemas. "
                + getFinalMessage(ACTIVE_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());
    }

    @Test
    public void timeReceivedTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidator.validateTimeReceived().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any PASSIVE schema must have a \"timeReceived\" field formatted "
                + "in DOUBLE. org.radarcns.monitor.test." + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        result = SchemaValidator.validateNotTimeReceived().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble("timeReceived")
                    .endRecord();

        result = SchemaValidator.validateTimeReceived().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidator.validateNotTimeReceived().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("\"timeReceived\" is allow only in PASSIVE schemas. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());
    }

    @Test
    public void fieldNameTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK + "value")
                .endRecord();

        result = SchemaValidator.validateFieldName().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Field name does not respect lowerCamelCase name convention. "
                + "It cannot contain any of the following values [value,Value]. "
                + "Please avoid abbreviations and write out the field name instead. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK)
                .endRecord();

        result = SchemaValidator.validateFieldName().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Field name does not respect lowerCamelCase name convention. "
                + "It cannot contain any of the following values [value,Value]. "
                + "Please avoid abbreviations and write out the field name instead. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .requiredString(FIELD_NUMBER_MOCK)
          .requiredString(SchemaValidator.TIME)
          .endRecord();

        result = SchemaValidator.validateFieldName(
                Collections.singleton(SchemaValidator.TIME)).apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Field name does not respect lowerCamelCase name convention. "
                + "It cannot contain any of the following values [value,Value]. "
                + "Please avoid abbreviations and write out the field name instead. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK,RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = SchemaValidator.validateFieldName().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredString(FIELD_NUMBER_MOCK)
              .requiredString(SchemaValidator.TIME)
              .endRecord();

        result = SchemaValidator.validateFieldName(
                Collections.singleton(FIELD_NUMBER_MOCK)).apply(schema);
        assertTrue(result.isValid());
    }

    @Test
    public void filedDocumentationTest() {
        Schema schema;
        ValidationResult result;

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\","
                + " \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation\"},"
                + "{\"name\": \"sourceId\", \"type\": \"string\"} ]}");

        result = SchemaValidator.validateFieldDocumentation().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Documentation is mandatory for any schema and field. The "
                + "documentation should report what is being measured, how, and what units or "
                + "ranges are applicable. Abbreviations and acronyms in the documentation should "
                + "be written out. The sentence must be ended by a point. Please add \"doc\" "
                + "property. org.radarcns.kafka.key.key is invalid."),
                result.getReason());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\", \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation.\"}]}");

        result = SchemaValidator.validateFieldDocumentation().apply(schema);
        assertTrue(result.isValid());
    }

    @Test
    public void schemaDocumentationTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .endRecord();

        result = SchemaValidator.validateSchemaDocumentation().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Documentation is mandatory for any schema and field. The "
                + "documentation should report what is being measured, how, and what units or "
                + "ranges are applicable. Abbreviations and acronyms in the documentation should "
                + "be written out. The sentence must be ended by a point. "
                + "Please add \"doc\" property. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .doc("Documentation.")
              .fields()
              .endRecord();

        result = SchemaValidator.validateSchemaDocumentation().apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void enumerationSymbolTest() {
        Schema schema;
        ValidationResult result;

        String enumName = "org.radarcns.monitor.application.ApplicationServerStatus";
        String connected = "CONNECTED";
        String unknown = "UNKNOWN";

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "DISCONNECTED", unknown);

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertTrue(result.isValid());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.monitor.application\", "
              + "\"type\": \"record\", \"name\": \"ApplicationServerStatus\", \"fields\": "
              + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
              + "\"enum\", \"symbols\": [\"CONNECTED\", \"NOT_CONNECTED\", \"UNKNOWN\"] } } ] }");

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "disconnected", unknown);

        result = SchemaValidator.validateEnumeration().apply(schema);

        String invalidMessage = "Enumerator items should be written in uppercase characters "
                + "separated by underscores. "
                + "org.radarcns.monitor.application.ApplicationServerStatus is invalid.";

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "Not_Connected", unknown);

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "NotConnected", unknown);

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.monitor.application\", "
              + "\"type\": \"record\", \"name\": \"ApplicationServerStatus\", \"fields\": "
              + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
              + "\"enum\", \"symbols\": [\"CONNECTED\", \"Not_Connected\", \"UNKNOWN\"] } } ] }");

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.monitor.application\", "
              + "\"type\": \"record\", \"name\": \"ApplicationServerStatus\", \"fields\": "
              + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
              + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] } } ] }");

        result = SchemaValidator.validateEnumeration().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());
    }

    private static String getFinalMessage(String nameSpace, String recordName) {
        return nameSpace.concat(".").concat(recordName).concat(INVALID_TEXT);
    }

}
