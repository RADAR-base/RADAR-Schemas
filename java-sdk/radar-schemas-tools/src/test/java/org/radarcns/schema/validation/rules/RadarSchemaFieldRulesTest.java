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
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationSupport;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.validation.rules.RadarSchemaFieldRules.FIELD_NAME_PATTERN;
import static org.radarcns.schema.validation.rules.Validator.matches;

/**
 * TODO.
 */
public class RadarSchemaFieldRulesTest {

    private static final String MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test";
    private static final String ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest";
    private static final String UNKNOWN_MOCK = "UNKNOWN";

    private static final String RECORD_NAME_MOCK = "RecordName";
    private static final String FIELD_NUMBER_MOCK = "Field1";
    private RadarSchemaFieldRules validator;
    private RadarSchemaRules schemaValidator;

    @Before
    public void setUp() throws IOException {
        validator = new RadarSchemaFieldRules();
        schemaValidator = new RadarSchemaRules(new ExcludeConfig(), validator);
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
    public void fieldNameRegex() {
        assertTrue(matches("interBeatInterval", FIELD_NAME_PATTERN));
        assertTrue(matches("x", FIELD_NAME_PATTERN));
        assertTrue(matches(RadarSchemaRules.TIME, FIELD_NAME_PATTERN));
        assertTrue(matches("subjectId", FIELD_NAME_PATTERN));
        assertTrue(matches("listOfSeveralThings", FIELD_NAME_PATTERN));
        assertFalse(matches("Time", FIELD_NAME_PATTERN));
        assertFalse(matches("E4Heart", FIELD_NAME_PATTERN));
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

        result = schemaValidator.fields(validator.validateFieldTypes(schemaValidator))
                .apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = schemaValidator.fields(validator.validateFieldTypes(schemaValidator))
                .apply(schema);

        assertEquals(0, result.count());
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

        result = schemaValidator.fields(validator.validateFieldName()).apply(schema);
        assertEquals(2, result.count());

        schema = SchemaBuilder
                .builder(MONITOR_NAME_SPACE_MOCK)
                .record(RECORD_NAME_MOCK)
                .fields()
                .requiredString(FIELD_NUMBER_MOCK)
                .endRecord();

        result = schemaValidator.fields(validator.validateFieldName()).apply(schema);
        assertEquals(1, result.count());

        schema = SchemaBuilder
              .builder(MONITOR_NAME_SPACE_MOCK)
              .record(RECORD_NAME_MOCK)
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = schemaValidator.fields(validator.validateFieldName()).apply(schema);
        assertEquals(0, result.count());
    }

    @Test
    public void fieldDocumentationTest() {
        Schema schema;
        Stream<ValidationException> result;

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\","
                + " \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation\"},"
                + "{\"name\": \"sourceId\", \"type\": \"string\"} ]}");

        result = schemaValidator.fields(validator.validateFieldDocumentation()).apply(schema);

        assertEquals(2, result.count());

        schema = new Parser().parse("{\"namespace\": \"org.radarcns.kafka.key\", "
                + "\"type\": \"record\", \"name\": \"key\", \"type\": \"record\", \"fields\": ["
                + "{\"name\": \"userId\", \"type\": \"string\" , \"doc\": \"Documentation.\"}]}");

        result = schemaValidator.fields(validator.validateFieldDocumentation()).apply(schema);
        assertEquals(0, result.count());
    }

    @Test
    public void defaultValueExceptionTest() {
        Stream<ValidationException> result = schemaValidator.fields(validator.validateDefault())
                .apply(SchemaBuilder.record(RECORD_NAME_MOCK)
                        .fields()
                        .name(FIELD_NUMBER_MOCK)
                        .type(SchemaBuilder.enumeration(ENUMERATOR_NAME_SPACE_MOCK)
                                .symbols("VAL", UNKNOWN_MOCK))
                        .noDefault()
                        .endRecord());

        assertEquals(1, result.count());
    }

    @Test
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    // TODO improve test after having define the default guideline
    public void defaultValueTest() {
        Schema schema;
        Stream<ValidationException> result;

        String schemaTxtInit = "{\"namespace\": \"org.radarcns.test\", "
                + "\"type\": \"record\", \"name\": \"TestRecord\", \"fields\": ";

        schema = new Parser().parse(schemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"UNKNOWN\" } ] }");

        result = schemaValidator.fields(validator.validateDefault()).apply(schema);

        assertEquals(0, result.count());

        schema = new Parser().parse(schemaTxtInit
            + "[ {\"name\": \"serverStatus\", \"type\": {\"name\": \"ServerStatus\", \"type\": "
            + "\"enum\", \"symbols\": [\"Connected\", \"NotConnected\", \"UNKNOWN\"] }, "
            + "\"default\": \"null\" } ] }");

        result = schemaValidator.fields(validator.validateDefault()).apply(schema);

        assertEquals(1, result.count());
    }
}
