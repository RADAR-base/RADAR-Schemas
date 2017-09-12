package org.radarcns.schema.validation;

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

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.validation.SchemaRepository.COMMONS_PATH;
import static org.radarcns.schema.Scope.ACTIVE;
import static org.radarcns.schema.Scope.MONITOR;
import static org.radarcns.schema.validation.SchemaValidationRoles.matches;

/**
 * TODO.
 */
public class SchemaValidationRolesTest {

    private static final String ACTIVE_NAME_SPACE_MOCK = "org.radarcns.active.test";
    private static final String MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test";
    private static final String ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest";
    private static final String UNKNOWN_MOCK = "UNKNOWN";

    private static final String RECORD_NAME_MOCK = "RecordName";
    private static final String FIELD_NUMBER_MOCK = "Field1";

    private static final String INVALID_TEXT = " is invalid.";

    @Test
    public void fileNameTest() {
        assertEquals("Questionnaire",
                ValidationSupport.getRecordName(Paths.get("/path/to/questionnaire.avsc")));
        assertEquals("ApplicationExternalTime",
                ValidationSupport.getRecordName(
                        Paths.get("/path/to/application_external_time.avsc")));
    }

    @Test
    public void nameSpaceRegex() {
        assertTrue(SchemaValidationRoles.matches("org.radarcns", SchemaValidationRoles.NAMESPACE_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Org.radarcns", SchemaValidationRoles.NAMESPACE_PATTERN));
        assertFalse(SchemaValidationRoles.matches("org.radarCns", SchemaValidationRoles.NAMESPACE_PATTERN));
        assertFalse(SchemaValidationRoles.matches(".org.radarcns", SchemaValidationRoles.NAMESPACE_PATTERN));
        assertFalse(SchemaValidationRoles.matches("org.radar-cns", SchemaValidationRoles.NAMESPACE_PATTERN));
        assertFalse(SchemaValidationRoles.matches("org.radarcns.empaticaE4", SchemaValidationRoles.NAMESPACE_PATTERN));
    }

    @Test
    public void recordNameRegex() {
        assertTrue(SchemaValidationRoles.matches("Questionnaire", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("EmpaticaE4Acceleration", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("Heart4Me", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("Heart4M", SchemaValidationRoles.RECORD_NAME_PATTERN));

        assertFalse(SchemaValidationRoles.matches("Heart4", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Heart4me", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Heart4ME", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("4Me", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("TTest", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("questionnaire", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("questionnaire4", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("questionnaire4Me", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("questionnaire4me", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("A4MM", SchemaValidationRoles.RECORD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Aaaa4MMaa", SchemaValidationRoles.RECORD_NAME_PATTERN));
    }

    @Test
    public void fieldNameRegex() {
        assertTrue(SchemaValidationRoles.matches("interBeatInterval", SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("x", SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches(SchemaValidationRoles.TIME, SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("subjectId", SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertTrue(SchemaValidationRoles.matches("listOfSeveralThings", SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Time", SchemaValidationRoles.FIELD_NAME_PATTERN));
        assertFalse(SchemaValidationRoles.matches("E4Heart", SchemaValidationRoles.FIELD_NAME_PATTERN));
    }

    @Test
    public void enumerationRegex() {
        assertTrue(SchemaValidationRoles.matches("PHQ8", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertTrue(SchemaValidationRoles.matches("HELLO", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertTrue(SchemaValidationRoles.matches("HELLOTHERE", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertTrue(SchemaValidationRoles.matches("HELLO_THERE", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Hello", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertFalse(SchemaValidationRoles.matches("hello", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertFalse(SchemaValidationRoles.matches("HelloThere", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertFalse(SchemaValidationRoles.matches("Hello_There", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
        assertFalse(SchemaValidationRoles.matches("HELLO.THERE", SchemaValidationRoles.ENUM_SYMBOL_PATTERN));
    }

    @Test
    public void nameSpaceTest() {
        Schema schema;
        ValidationResult result;
        Path path;
        Path root;

        schema = SchemaBuilder
                    .builder("org.radarcns.active.questionnaire")
                    .record("Questionnaire")
                    .fields()
                    .endRecord();

        root = ACTIVE.getPath(COMMONS_PATH);
        assertNotNull(root);
        path = root.resolve("questionnaire/questionnaire.avsc");

        result = SchemaValidationRoles.validateNameSpace(path, ACTIVE).apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder("org.radar-cns.monitors.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        root = MONITOR.getPath(COMMONS_PATH);
        assertNotNull(root);
        path = root.resolve("test/record_name.avsc");
        result = SchemaValidationRoles.validateNameSpace(path, MONITOR).apply(schema);

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

        result = SchemaValidationRoles.validateNameSpace(path, MONITOR).apply(schema);

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

        result = SchemaValidationRoles.validateNameSpace(path, MONITOR).apply(schema);

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

        result = SchemaValidationRoles.validateRecordName(
                Paths.get("/path/to/schema.avsc")).apply(schema);

        assertTrue(result.isValid());

        String fieldName = "EmpaticaE4Aceleration";
        Path filePath = Paths.get("/path/to/empatica_e4_acceleration.avsc");

        schema = SchemaBuilder
                    .builder("org.radarcns.passive.empatica")
                    .record(fieldName)
                    .fields()
                    .endRecord();

        result = SchemaValidationRoles.validateRecordName(filePath).apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Record name must be the conversion of the .avsc file name in "
                + "UpperCamelCase and must explicitly contain the device name. "
                + "The expected value is EmpaticaE4Acceleration\". org.radarcns.passive.empatica."
                + fieldName + INVALID_TEXT),
                result.getReason());

        result = SchemaValidationRoles.validateRecordName(filePath, true).apply(schema);

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

        result = SchemaValidationRoles.validateFields().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Avro Record must have field list. "
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)), result.getReason());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = SchemaValidationRoles.validateFields().apply(schema);

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

        result = SchemaValidationRoles.validateTime().apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Any schema representing collected data must have a \"time\" field"
                + " formatted in DOUBLE. org.radarcns.time.test." + RECORD_NAME_MOCK
                + INVALID_TEXT),
                result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble(SchemaValidationRoles.TIME)
                    .endRecord();

        result = SchemaValidationRoles.validateTime().apply(schema);

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

        result = SchemaValidationRoles.validateTimeCompleted().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any ACTIVE schema must have a \"timeCompleted\" field formatted "
                + "in DOUBLE. org.radarcns.active.test." + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        result = SchemaValidationRoles.validateNotTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                      .builder(ACTIVE_NAME_SPACE_MOCK)
                      .record(RECORD_NAME_MOCK)
                      .fields()
                      .requiredDouble("timeCompleted")
                      .endRecord();

        result = SchemaValidationRoles.validateTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidationRoles.validateNotTimeCompleted().apply(schema);
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

        result = SchemaValidationRoles.validateTimeReceived().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any PASSIVE schema must have a \"timeReceived\" field formatted "
                + "in DOUBLE. org.radarcns.monitor.test." + RECORD_NAME_MOCK + INVALID_TEXT),
                result.getReason());

        result = SchemaValidationRoles.validateNotTimeReceived().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble("timeReceived")
                    .endRecord();

        result = SchemaValidationRoles.validateTimeReceived().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidationRoles.validateNotTimeReceived().apply(schema);
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

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertFalse(result.isValid());

        String message = "Field name does not respect lowerCamelCase name convention. "
                + "It cannot contain any of the following values [value,Value,val,Val]. "
                + "Please avoid abbreviations and write out the field name instead. ";

        assertEquals(Optional.of(message
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK)
                .endRecord();

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of(message
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK, RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .requiredString(FIELD_NUMBER_MOCK)
          .requiredString(SchemaValidationRoles.TIME)
          .endRecord();

        result = SchemaValidationRoles.validateFieldName(
                Collections.singleton(SchemaValidationRoles.TIME)).apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of(message
                + getFinalMessage(MONITOR_NAME_SPACE_MOCK,RECORD_NAME_MOCK)),
                result.getReason());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredString(FIELD_NUMBER_MOCK)
              .requiredString(SchemaValidationRoles.TIME)
              .endRecord();

        result = SchemaValidationRoles.validateFieldName(
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

        result = SchemaValidationRoles.validateFieldDocumentation().apply(schema);

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

        result = SchemaValidationRoles.validateFieldDocumentation().apply(schema);
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

        result = SchemaValidationRoles.validateSchemaDocumentation().apply(schema);

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

        result = SchemaValidationRoles.validateSchemaDocumentation().apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void enumerationSymbolsTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("TEST", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateSymbols().apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK).symbols();

        result = SchemaValidationRoles.validateSymbols().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Avro Enumerator must have symbol list. "
                + ENUMERATOR_NAME_SPACE_MOCK + INVALID_TEXT), result.getReason());
    }

    @Test
    public void enumerationSymbolTest() {
        Schema schema;
        ValidationResult result;

        String enumName = "org.radarcns.monitor.application.ApplicationServerStatus";
        String connected = "CONNECTED";

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "DISCONNECTED", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertTrue(result.isValid());

        String schemaTxtInit = "{\"namespace\": \"org.radarcns.monitor.application\", "
                + "\"type\": \"record\", \"name\": \"ApplicationServerStatus\", \"fields\": "
                + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
                + "\"enum\", \"symbols\": [";

        String schemaTxtEnd = "] } } ] }";

        schema = new Parser().parse(schemaTxtInit.concat(
                "\"CONNECTED\", \"NOT_CONNECTED\", \"" + UNKNOWN_MOCK + "\"".concat(schemaTxtEnd)));

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "disconnected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        String invalidMessage = "Enumerator items should be written in uppercase characters "
                + "separated by underscores. "
                + "org.radarcns.monitor.application.ApplicationServerStatus is invalid.";

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "Not_Connected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "NotConnected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = new Parser().parse(schemaTxtInit.concat(
                "\"CONNECTED\", \"Not_Connected\", \"" + UNKNOWN_MOCK + "\"".concat(schemaTxtEnd)));

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = new Parser().parse(schemaTxtInit.concat(
                "\"Connected\", \"NotConnected\", \"" + UNKNOWN_MOCK + "\"".concat(schemaTxtEnd)));

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());
    }

    @Test
    public void unknownSymbolTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("VALUE", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateUnknownSymbol().apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("FIELD", "UN_KNOWN");

        result = SchemaValidationRoles.validateUnknownSymbol().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of("Enumerator must contain the \"UNKNOWN\" symbol. It is "
                + "useful to specify default value for a field using type equals to \"enum\". "
                + ENUMERATOR_NAME_SPACE_MOCK + INVALID_TEXT),
                result.getReason());
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultValueExceptionTest() {
        SchemaValidationRoles.validateDefault().apply(
                SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
                    .symbols("VAL", UNKNOWN_MOCK));
    }

    @Test
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    // TODO improve test after having define the default guideline
    public void defaultValueTest() {
        //String namespace = "org.radarcns.test";
        //String recordName = "TestRecord";

        Schema schema;
        ValidationResult result;

        /*schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableDouble("nullableDouble", Double.NaN)
            .nullableFloat("nullableFloat", Float.NaN)
            .nullableInt("nullableIntMin", Integer.MIN_VALUE)
            .nullableInt("nullableIntMax", Integer.MAX_VALUE)
            .nullableLong("nullableLongMin", Long.MIN_VALUE)
            .nullableLong("nullableLongMax", Long.MAX_VALUE)
            .nullableString("nullableString", null)
            .nullableBoolean("nullableBoolean", false) //check with text schema
            .nullableBytes("nullableBytes", new byte[1])  //check with text schema
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertTrue(result.isValid());*/

        String scemaTxtInit = "{\"namespace\": \"org.radarcns.test\", "
                + "\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": ";

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBytes\", \"type\": [ \"null\", \"bytes\"], "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableDouble("nullableDouble", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);*/

        /*assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableInt("nullableInt", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableLong("nullableLong", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"UNKNOWN\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertTrue(result.isValid());

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        String invalidMessage = "Any NULLABLE Avro field must specify a default value. "
                + "The allowed default values are: \"UNKNOWN\" for ENUMERATION, \"MIN_VALUE\" or "
                + "\"MAX_VALUE\" for nullable int and long, \"NaN\" for nullable float and double, "
                + "\"true\" or \"false\" for nullable boolean, \"byte[]\" or \"null\" for bytes, "
                + "and \"null\" for all the other cases. org.radarcns.test.TestRecord"
                + INVALID_TEXT;

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBoolean\", \"type\": [ \"null\", \"boolean\"], "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isValid());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/
    }

    private static String getFinalMessage(String nameSpace, String recordName) {
        return nameSpace.concat(".").concat(recordName).concat(INVALID_TEXT);
    }

}
