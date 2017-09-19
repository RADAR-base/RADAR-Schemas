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

package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.radarcns.schema.validation.SchemaValidator;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationSupport;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.SchemaRepository.COMMONS_PATH;
import static org.radarcns.schema.Scope.ACTIVE;
import static org.radarcns.schema.Scope.MONITOR;
import static org.radarcns.schema.Scope.PASSIVE;
import static org.radarcns.schema.specification.SourceCatalogue.BASE_PATH;
import static org.radarcns.schema.validation.rules.RadarSchemaValidationRules.ENUM_SYMBOL_PATTERN;
import static org.radarcns.schema.validation.rules.RadarSchemaValidationRules.FIELD_NAME_PATTERN;
import static org.radarcns.schema.validation.rules.RadarSchemaValidationRules.NAMESPACE_PATTERN;
import static org.radarcns.schema.validation.rules.RadarSchemaValidationRules.RECORD_NAME_PATTERN;
import static org.radarcns.schema.validation.rules.Validator.matches;

/**
 * TODO.
 */
public class RadarSchemaValidationRulesTest {

    private static final String ACTIVE_NAME_SPACE_MOCK = "org.radarcns.active.test";
    private static final String MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test";
    private static final String ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest";
    private static final String UNKNOWN_MOCK = "UNKNOWN";

    private static final String RECORD_NAME_MOCK = "RecordName";
    private static final String FIELD_NUMBER_MOCK = "Field1";
    private RadarSchemaValidationRules validator;
    private ExcludeConfig config;

    @Before
    public void setUp() throws IOException {
        config = new ExcludeConfig();
        validator = new RadarSchemaValidationRules(BASE_PATH, config);
    }

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
        assertTrue(matches("Heart4", RECORD_NAME_PATTERN));
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
        assertTrue(matches(RadarSchemaValidationRules.TIME, FIELD_NAME_PATTERN));
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

        Path root = ACTIVE.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);

        Stream<ValidationException> result = validator.validateNameSpace()
                .apply(schema);

        assertEquals(0, result.count());
    }

    @Test
    public void nameSpaceInvalidDashTest() {
        Schema schema = SchemaBuilder
                .builder("org.radar-cns.monitors.test")
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        Path root = MONITOR.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Stream<ValidationException> result = validator.validateNameSpace()
                .apply(schema);

        assertEquals(1, result.count());

    }

    @Test
    public void nameSpaceInvalidPlural() {
        Schema schema = SchemaBuilder
                .builder("org.radarcns.monitors.test")
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        Path root = MONITOR.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Stream<ValidationException> result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, MONITOR, path));

        assertEquals(1, result.count());
    }

    @Test
    public void nameSpaceInvalidLastPartPlural() {

        Schema schema = SchemaBuilder
                    .builder("org.radarcns.monitor.tests")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .endRecord();

        Path root = MONITOR.getPath(BASE_PATH.resolve(COMMONS_PATH));
        assertNotNull(root);
        Path path = root.resolve("test/record_name.avsc");
        Stream<ValidationException> result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, MONITOR, path));

        assertEquals(1, result.count());
    }

    @Test
    public void recordNameTest() {
        Schema schema = SchemaBuilder
                    .builder("org.radarcns.active.testactive")
                    .record("Schema")
                    .fields()
                    .endRecord();

        Stream<ValidationException> result = validator.validateName()
                .apply(schema);

        assertEquals(0, result.count());

        // misspell aceleration
        String fieldName = "EmpaticaE4Aceleration";
        Path filePath = Paths.get("/path/to/empatica_e4_acceleration.avsc");

        schema = SchemaBuilder
                    .builder("org.radarcns.passive.empatica")
                    .record(fieldName)
                    .fields()
                    .endRecord();

        result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, PASSIVE, filePath));

        assertEquals(2, result.count());

        fieldName = "EmpaticaE4Acceleration";
        filePath = BASE_PATH.resolve("commons/passive/empatica/empatica_e4_acceleration.avsc");

        schema = SchemaBuilder
                .builder("org.radarcns.passive.empatica")
                .record(fieldName)
                .fields()
                .endRecord();

        result = validator.validateSchemaLocation()
                .apply(new SchemaMetadata(schema, PASSIVE, filePath));

        assertEquals("", SchemaValidator.format(result));
    }

    @Test
    public void fieldsTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .endRecord();

        result = validator.fields(validator.validateFieldTypes()).apply(new SchemaMetadata(schema));

        assertEquals(1, result.count());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = validator.fields(validator.validateFieldTypes())
                .apply(new SchemaMetadata(schema));

        assertEquals(0, result.count());
    }

    @Test
    public void timeTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("string")
                    .endRecord();

        result = validator.validateTime().apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
                    .builder("org.radarcns.time.test")
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble(RadarSchemaValidationRules.TIME)
                    .endRecord();

        result = validator.validateTime().apply(schema);

        assertEquals(0, result.count());
    }

    @Test
    public void timeCompletedTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
                    .builder(ACTIVE_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = validator.validateTimeCompleted().apply(schema);
        assertEquals(1, result.count());

        result = validator.validateNotTimeCompleted().apply(schema);
        assertEquals(0, result.count());

        schema = SchemaBuilder
                      .builder(ACTIVE_NAME_SPACE_MOCK)
                      .record(RECORD_NAME_MOCK)
                      .fields()
                      .requiredDouble("timeCompleted")
                      .endRecord();

        result = validator.validateTimeCompleted().apply(schema);
        assertEquals(0, result.count());

        result = validator.validateNotTimeCompleted().apply(schema);
        assertEquals(1, result.count());
    }

    @Test
    public void timeReceivedTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = validator.validateTimeReceived().apply(schema);
        assertEquals(1, result.count());

        result = validator.validateNotTimeReceived().apply(schema);
        assertEquals(0, result.count());

        schema = SchemaBuilder
                    .builder(MONITOR_NAME_SPACE_MOCK)
                    .record(RECORD_NAME_MOCK)
                    .fields()
                    .requiredDouble("timeReceived")
                    .endRecord();

        result = validator.validateTimeReceived().apply(schema);
        assertEquals(0, result.count());

        result = validator.validateNotTimeReceived().apply(schema);
        assertEquals(1, result.count());
    }

    @Test
    public void fieldNameTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK + "value")
                .endRecord();

        result = validator.fields(validator.validateFieldName()).apply(new SchemaMetadata(schema));
        assertEquals(1, result.count());

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK)
                .endRecord();

        result = validator.fields(validator.validateFieldName()).apply(new SchemaMetadata(schema));
        assertEquals(1, result.count());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .requiredString(FIELD_NUMBER_MOCK)
          .requiredString(RadarSchemaValidationRules.TIME)
          .endRecord();

        result = validator.fields(validator.validateFieldName(
                s -> RadarSchemaValidationRules.TIME.equalsIgnoreCase(s.getField().name())))
                .apply(new SchemaMetadata(schema));
        assertEquals(1, result.count());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = validator.fields(validator.validateFieldName()).apply(new SchemaMetadata(schema));
        assertEquals(0, result.count());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredString(FIELD_NUMBER_MOCK)
              .requiredString(RadarSchemaValidationRules.TIME)
              .endRecord();

        result = validator.fields(validator.validateFieldName(
                        s -> FIELD_NUMBER_MOCK.equalsIgnoreCase(s.getField().name())))
                        .apply(new SchemaMetadata(schema));
        assertEquals(0, result.count());
    }

    @Test
    public void filedDocumentationTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\","
                + " \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation\"},"
                + "{\"name\": \"sourceId\", \"type\": \"string\"} ]}");

        result = validator.fields(validator.validateFieldDocumentation())
                .apply(new SchemaMetadata(schema));

        assertEquals(2, result.count());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\", \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation.\"}]}");

        result = validator.fields(validator.validateFieldDocumentation())
                .apply(new SchemaMetadata(schema));
        assertEquals(0, result.count());
    }

    @Test
    public void schemaDocumentationTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .endRecord();

        result = validator.validateSchemaDocumentation().apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .doc("Documentation.")
              .fields()
              .endRecord();

        result = validator.validateSchemaDocumentation().apply(schema);

        assertEquals(0, result.count());
    }

    @Test
    public void enumerationSymbolsTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
            .symbols("TEST", UNKNOWN_MOCK);

        result = validator.validateSymbols().apply(schema);

        assertEquals(0, result.count());

        schema = SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK).symbols();

        result = validator.validateSymbols().apply(schema);

        assertEquals(1, result.count());
    }

    @Test
    public void enumerationSymbolTest() {
        Schema schema;
        Stream<ValidationException> result;

        String enumName = "org.radarcns.monitor.application.ApplicationServerStatus";
        String connected = "CONNECTED";

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "DISCONNECTED", UNKNOWN_MOCK);

        result = validator.validateSymbols().apply(schema);

        assertEquals(0, result.count());

        String schemaTxtInit = "{\"namespace\": \"org.radarcns.monitor.application\", "
                + "\"name\": \"ServerStatus\", \"type\": "
                + "\"enum\", \"symbols\": [";

        String schemaTxtEnd = "] }";

        schema = new Parser().parse(schemaTxtInit
                + "\"CONNECTED\", \"NOT_CONNECTED\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = validator.validateSymbols().apply(schema);

        assertEquals(0, result.count());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "disconnected", UNKNOWN_MOCK);

        result = validator.validateSymbols().apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "Not_Connected", UNKNOWN_MOCK);

        result = validator.validateSymbols().apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
              .enumeration(enumName)
              .symbols(connected, "NotConnected", UNKNOWN_MOCK);

        result = validator.validateSymbols().apply(schema);

        assertEquals(1, result.count());

        schema = new Parser().parse(schemaTxtInit
                + "\"CONNECTED\", \"Not_Connected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = validator.validateSymbols().apply(schema);

        assertEquals(1, result.count());

        schema = new Parser().parse(schemaTxtInit
                + "\"Connected\", \"NotConnected\", \"" + UNKNOWN_MOCK + "\"" + schemaTxtEnd);

        result = validator.validateSymbols().apply(schema);

        assertEquals(2, result.count());
    }

    @Test
    public void defaultValueExceptionTest() {
        Stream<ValidationException> result = validator.fields(validator.validateDefault()).apply(
                new SchemaMetadata(SchemaBuilder.record(RECORD_NAME_MOCK)
                        .fields()
                        .name(FIELD_NUMBER_MOCK)
                        .type(SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
                                .symbols("VAL", UNKNOWN_MOCK))
                        .noDefault()
                        .endRecord()));

        assertEquals(1, result.count());
    }

    @Test
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    // TODO improve test after having define the default guideline
    public void defaultValueTest() {
        //String namespace = "org.radarcns.test";
        //String recordName = "TestRecord";

        Schema schema;
        Stream<ValidationException> result;

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

        result = validator.validateDefault().apply(schema);

        assertEquals(0, result.count());*/

        String scemaTxtInit = "{\"namespace\": \"org.radarcns.test\", "
                + "\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": ";

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBytes\", \"type\": [ \"null\", \"bytes\"], "
            + "\"default\": \"null\" } ] }");

        result = validator.validateDefault().apply(schema);

        assertEquals(0, result.count());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableDouble("nullableDouble", -1)
            .endRecord();

        result = validator.validateDefault().apply(schema);*/

        /*assertEquals(1, result.count());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableInt("nullableInt", -1)
            .endRecord();

        result = validator.validateDefault().apply(schema);

        assertEquals(1, result.count());
        assertEquals(Optional.of(invalidMessage), result.getReason());

        schema = SchemaBuilder
            .builder(namespace)
            .record(recordName)
            .fields()
            .nullableLong("nullableLong", -1)
            .endRecord();

        result = validator.validateDefault().apply(schema);

        assertEquals(1, result.count());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"UNKNOWN\" } ] }");

        result = validator.fields(validator.validateDefault()).apply(new SchemaMetadata(schema));

        assertEquals(0, result.count());

        schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"null\" } ] }");

        result = validator.fields(validator.validateDefault()).apply(new SchemaMetadata(schema));

        assertEquals(1, result.count());

        /*schema = new Parser().parse(scemaTxtInit
            + "[ {\"name\": \"nullableBoolean\", \"type\": [ \"null\", \"boolean\"], "
            + "\"default\": \"null\" } ] }");

        result = validator.validateDefault().apply(schema);

        assertEquals(1, result.count());
        assertEquals(Optional.of(invalidMessage), result.getReason());*/
    }
}
