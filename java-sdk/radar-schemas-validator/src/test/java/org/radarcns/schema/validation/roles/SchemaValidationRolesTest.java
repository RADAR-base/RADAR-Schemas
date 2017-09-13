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

package org.radarcns.schema.validation.roles;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationSupport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.Scope.ACTIVE;
import static org.radarcns.schema.Scope.MONITOR;
import static org.radarcns.schema.validation.SchemaRepository.COMMONS_PATH;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.ENUM_SYMBOL_PATTERN;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.FIELD_NAME_PATTERN;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.NAMESPACE_PATTERN;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.RECORD_NAME_PATTERN;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.validateNameSpace;
import static org.radarcns.schema.validation.roles.Validator.matches;

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
        assertTrue(matches("org.radarcns", NAMESPACE_PATTERN));
        assertFalse(matches("Org.radarcns", NAMESPACE_PATTERN));
        assertFalse(matches("org.radarCns", NAMESPACE_PATTERN));
        assertFalse(matches(".org.radarcns", NAMESPACE_PATTERN));
        assertFalse(matches("org.radar-cns", NAMESPACE_PATTERN));
        assertFalse(matches("org.radarcns.empaticaE4", NAMESPACE_PATTERN));
    }

    @Test
    public void recordNameRegex() {
        assertTrue(matches("Questionnaire", RECORD_NAME_PATTERN));
        assertTrue(matches("EmpaticaE4Acceleration", RECORD_NAME_PATTERN));
        assertTrue(matches("Heart4Me", RECORD_NAME_PATTERN));
        assertTrue(matches("Heart4M", RECORD_NAME_PATTERN));

        assertFalse(matches("Heart4", RECORD_NAME_PATTERN));
        assertFalse(matches("Heart4me", RECORD_NAME_PATTERN));
        assertFalse(matches("Heart4ME", RECORD_NAME_PATTERN));
        assertFalse(matches("4Me", RECORD_NAME_PATTERN));
        assertFalse(matches("TTest", RECORD_NAME_PATTERN));
        assertFalse(matches("questionnaire", RECORD_NAME_PATTERN));
        assertFalse(matches("questionnaire4", RECORD_NAME_PATTERN));
        assertFalse(matches("questionnaire4Me", RECORD_NAME_PATTERN));
        assertFalse(matches("questionnaire4me", RECORD_NAME_PATTERN));
        assertFalse(matches("A4MM", RECORD_NAME_PATTERN));
        assertFalse(matches("Aaaa4MMaa", RECORD_NAME_PATTERN));
    }

    @Test
    public void fieldNameRegex() {
        assertTrue(matches("interBeatInterval", FIELD_NAME_PATTERN));
        assertTrue(matches("x", FIELD_NAME_PATTERN));
        assertTrue(matches(SchemaValidationRoles.TIME, FIELD_NAME_PATTERN));
        assertTrue(matches("subjectId", FIELD_NAME_PATTERN));
        assertTrue(matches("listOfSeveralThings", FIELD_NAME_PATTERN));
        assertFalse(matches("Time", FIELD_NAME_PATTERN));
        assertFalse(matches("E4Heart", FIELD_NAME_PATTERN));
    }

    @Test
    public void enumerationRegex() {
        assertTrue(matches("PHQ8", ENUM_SYMBOL_PATTERN));
        assertTrue(matches("HELLO", ENUM_SYMBOL_PATTERN));
        assertTrue(matches("HELLOTHERE", ENUM_SYMBOL_PATTERN));
        assertTrue(matches("HELLO_THERE", ENUM_SYMBOL_PATTERN));
        assertFalse(matches("Hello", ENUM_SYMBOL_PATTERN));
        assertFalse(matches("hello", ENUM_SYMBOL_PATTERN));
        assertFalse(matches("HelloThere", ENUM_SYMBOL_PATTERN));
        assertFalse(matches("Hello_There", ENUM_SYMBOL_PATTERN));
        assertFalse(matches("HELLO.THERE", ENUM_SYMBOL_PATTERN));
    }

    @Test
    public void nameSpaceTest() {
        Schema schema = SchemaBuilder
                .builder("org.radarcns.active.questionnaire")
                .record("Questionnaire")
                .fields()
                .endRecord();

        Path root = ACTIVE.getPath(COMMONS_PATH);
        assertNotNull(root);
        Path path = root.resolve("questionnaire/questionnaire.avsc");

        Collection<ValidationException> result =validateNameSpace(path, ACTIVE).apply(schema);

        assertTrue(result.isEmpty());
    }

    @Test
    public void nameSpaceInvalidDashTest() {
        Schema schema = SchemaBuilder
                .builder("org.radar-cns.monitors.test")
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        Path root = MONITOR.getPath(COMMONS_PATH);
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Collection<ValidationException> result =validateNameSpace(path, MONITOR).apply(schema);

        assertFalse(result.isEmpty());

    }

    @Test
    public void nameSpaceInvalidPlural() {
        Schema schema = SchemaBuilder
                .builder("org.radarcns.monitors.test")
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        Path root = MONITOR.getPath(COMMONS_PATH);
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Collection<ValidationException> result =validateNameSpace(path, MONITOR).apply(schema);

        assertFalse(result.isEmpty());
    }

    @Test
    public void nameSpaceInvalidLastPartPlural() {

        Schema schema = SchemaBuilder
                    .builder("org.radarcns.monitor.tests")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        Path root = MONITOR.getPath(COMMONS_PATH);
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Collection<ValidationException> result =validateNameSpace(path, MONITOR).apply(schema);

        assertFalse(result.isEmpty());
    }

    @Test
    public void recordNameTest() {
        Schema schema = SchemaBuilder
                    .builder("org.radarcns.active.testactive")
                    .record("Schema")
                    .fields()
                    .endRecord();

        Collection<ValidationException> result = SchemaValidationRoles.validateRecordName(
                Paths.get("/path/to/schema.avsc")).apply(schema);

        assertTrue(result.isEmpty());

        String fieldName = "EmpaticaE4Aceleration";
        Path filePath = Paths.get("/path/to/empatica_e4_acceleration.avsc");

        schema = SchemaBuilder
                    .builder("org.radarcns.passive.empatica")
                    .record(fieldName)
                    .fields()
                    .endRecord();

        result = SchemaValidationRoles.validateRecordName(filePath).apply(schema);

        assertFalse(result.isEmpty());

        result = SchemaValidationRoles.validateRecordName(filePath, true).apply(schema);

        assertTrue(result.isEmpty());
    }

    @Test
    public void fieldsTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        result = SchemaValidationRoles.validateFields().apply(schema);

        assertFalse(result.isEmpty());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = SchemaValidationRoles.validateFields().apply(schema);

        assertTrue(result.isEmpty());
    }

    @Test
    public void timeTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("string")
                    .endRecord();

        result = SchemaValidationRoles.validateTime().apply(schema);

        assertFalse(result.isEmpty());

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble(SchemaValidationRoles.TIME)
                    .endRecord();

        result = SchemaValidationRoles.validateTime().apply(schema);

        assertTrue(result.isEmpty());
    }

    @Test
    public void timeCompletedTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
                    .builder(ACTIVE_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidationRoles.validateTimeCompleted().apply(schema);
        assertFalse(result.isEmpty());

        result = SchemaValidationRoles.validateNotTimeCompleted().apply(schema);
        assertTrue(result.isEmpty());

        schema = SchemaBuilder
                      .builder(ACTIVE_NAME_SPACE_MOCK)
                      .record(RECORD_NAME_MOCK)
                      .fields()
                      .requiredDouble("timeCompleted")
                      .endRecord();

        result = SchemaValidationRoles.validateTimeCompleted().apply(schema);
        assertTrue(result.isEmpty());

        result = SchemaValidationRoles.validateNotTimeCompleted().apply(schema);
        assertFalse(result.isEmpty());
    }

    @Test
    public void timeReceivedTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidationRoles.validateTimeReceived().apply(schema);
        assertFalse(result.isEmpty());

        result = SchemaValidationRoles.validateNotTimeReceived().apply(schema);
        assertTrue(result.isEmpty());

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble("timeReceived")
                    .endRecord();

        result = SchemaValidationRoles.validateTimeReceived().apply(schema);
        assertTrue(result.isEmpty());

        result = SchemaValidationRoles.validateNotTimeReceived().apply(schema);
        assertFalse(result.isEmpty());
    }

    @Test
    public void fieldNameTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK + "value")
                .endRecord();

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertFalse(result.isEmpty());

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK)
                .endRecord();

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertFalse(result.isEmpty());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .requiredString(FIELD_NUMBER_MOCK)
          .requiredString(SchemaValidationRoles.TIME)
          .endRecord();

        result = SchemaValidationRoles.validateFieldName(
                Collections.singleton(SchemaValidationRoles.TIME)).apply(schema);
        assertFalse(result.isEmpty());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = SchemaValidationRoles.validateFieldName().apply(schema);
        assertTrue(result.isEmpty());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredString(FIELD_NUMBER_MOCK)
              .requiredString(SchemaValidationRoles.TIME)
              .endRecord();

        result = SchemaValidationRoles.validateFieldName(
                Collections.singleton(FIELD_NUMBER_MOCK)).apply(schema);
        assertTrue(result.isEmpty());
    }

    @Test
    public void filedDocumentationTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\","
                + " \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation\"},"
                + "{\"name\": \"sourceId\", \"type\": \"string\"} ]}");

        result = SchemaValidationRoles.validateFieldDocumentation().apply(schema);

        assertFalse(result.isEmpty());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\", \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation.\"}]}");

        result = SchemaValidationRoles.validateFieldDocumentation().apply(schema);
        assertTrue(result.isEmpty());
    }

    @Test
    public void schemaDocumentationTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .endRecord();

        result = SchemaValidationRoles.validateSchemaDocumentation().apply(schema);

        assertFalse(result.isEmpty());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .doc("Documentation.")
              .fields()
              .endRecord();

        result = SchemaValidationRoles.validateSchemaDocumentation().apply(schema);

        assertTrue(result.isEmpty());
    }

    @Test
    public void enumerationSymbolsTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("TEST", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateSymbols().apply(schema);

        assertTrue(result.isEmpty());

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK).symbols();

        result = SchemaValidationRoles.validateSymbols().apply(schema);

        assertFalse(result.isEmpty());
    }

    @Test
    public void enumerationSymbolTest() {
        Schema schema;
        Collection<ValidationException> result;

        String enumName = "org.radarcns.monitor.application.ApplicationServerStatus";
        String connected = "CONNECTED";

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "DISCONNECTED", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertTrue(result.isEmpty());

        String schemaTxtInit = "{\"namespace\": \"org.radarcns.monitor.application\", "
                + "\"type\": \"record\", \"name\": \"ApplicationServerStatus\", \"fields\": "
                + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
                + "\"enum\", \"symbols\": [";

        String schemaTxtEnd = "] } } ] }";

        schema = new Parser().parse(schemaTxtInit
                + "\"CONNECTED\", \"NOT_CONNECTED\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertTrue(result.isEmpty());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "disconnected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isEmpty());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "Not_Connected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isEmpty());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "NotConnected", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isEmpty());

        schema = new Parser().parse(schemaTxtInit
                + "\"CONNECTED\", \"Not_Connected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isEmpty());

        schema = new Parser().parse(schemaTxtInit
                + "\"Connected\", \"NotConnected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = SchemaValidationRoles.validateEnumerationSymbols().apply(schema);

        assertFalse(result.isEmpty());
    }

    @Test
    public void unknownSymbolTest() {
        Schema schema;
        Collection<ValidationException> result;

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("VALUE", UNKNOWN_MOCK);

        result = SchemaValidationRoles.validateUnknownSymbol().apply(schema);

        assertTrue(result.isEmpty());

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("FIELD", "UN_KNOWN");

        result = SchemaValidationRoles.validateUnknownSymbol().apply(schema);

        assertFalse(result.isEmpty());
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
        Collection<ValidationException> result;

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

        assertTrue(result.isEmpty());*/

        String scemaTxtInit = "{\"namespace\": \"org.radarcns.test\", "
                + "\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": ";

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBytes\", \"type\": [ \"null\", \"bytes\"], "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertTrue(result.isEmpty());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableDouble("nullableDouble", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);*/

        /*assertFalse(result.isEmpty());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableInt("nullableInt", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isEmpty());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableLong("nullableLong", -1)
            .endRecord();

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isEmpty());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"UNKNOWN\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertTrue(result.isEmpty());

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isEmpty());

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBoolean\", \"type\": [ \"null\", \"boolean\"], "
            + "\"default\": \"null\" } ] }");

        result = SchemaValidationRoles.validateDefault().apply(schema);

        assertFalse(result.isEmpty());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/
    }
}
