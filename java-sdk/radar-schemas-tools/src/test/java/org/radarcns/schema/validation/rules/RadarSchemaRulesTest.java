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
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.validation.rules.RadarSchemaRules.ENUM_SYMBOL_PATTERN;
import static org.radarcns.schema.validation.rules.RadarSchemaRules.NAMESPACE_PATTERN;
import static org.radarcns.schema.validation.rules.RadarSchemaRules.RECORD_NAME_PATTERN;
import static org.radarcns.schema.validation.rules.Validator.matches;

/**
 * TODO.
 */
public class RadarSchemaRulesTest {

    private static final String ACTIVE_NAME_SPACE_MOCK = "org.radarcns.active.test";
    private static final String MONITOR_NAME_SPACE_MOCK = "org.radarcns.monitor.test";
    private static final String ENUMERATOR_NAME_SPACE_MOCK = "org.radarcns.test.EnumeratorTest";
    private static final String UNKNOWN_MOCK = "UNKNOWN";

    private static final String RECORD_NAME_MOCK = "RecordName";
    private RadarSchemaRules validator;

    @Before
    public void setUp() throws IOException {
        ExcludeConfig config = new ExcludeConfig();
        validator = new RadarSchemaRules(config);
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

        Stream<ValidationException> result = validator.validateNameSpace()
                .apply(schema);

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

        result = validator.fields(validator.getFieldRules().validateFieldTypes(validator))
                .apply(schema);

        assertEquals(1, result.count());

        schema = SchemaBuilder
          .builder(MONITOR_NAME_SPACE_MOCK)
          .record(RECORD_NAME_MOCK)
          .fields()
          .optionalBoolean("optional")
          .endRecord();

        result = validator.fields(validator.getFieldRules().validateFieldTypes(validator))
                .apply(schema);

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
                    .requiredDouble(RadarSchemaRules.TIME)
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
    public void testUniqueness() {
        final String prefix = "{\"namespace\": \"org.radarcns.monitor.application\", "
                + "\"name\": \"";
        final String infix = "\", \"type\": \"enum\", \"symbols\": ";
        final char suffix = '}';

        Schema schema = new Parser().parse(prefix + "ServerStatus"
                + infix + "[\"A\", \"B\"]" + suffix);
        Stream<ValidationException> result = validator.validateUniqueness().apply(schema);
        assertEquals(0, result.count());
        result = validator.validateUniqueness().apply(schema);
        assertEquals(0, result.count());

        Schema schemaAlt = new Parser().parse(prefix + "ServerStatus"
                + infix + "[\"A\", \"B\", \"C\"]" + suffix);
        result = validator.validateUniqueness().apply(schemaAlt);
        assertEquals(1, result.count());
        result = validator.validateUniqueness().apply(schemaAlt);
        assertEquals(1, result.count());

        Schema schema2 = new Parser().parse(prefix + "ServerStatus2"
                + infix + "[\"A\", \"B\"]" + suffix);
        result = validator.validateUniqueness().apply(schema2);
        assertEquals(0, result.count());

        Schema schema3 = new Parser().parse(prefix + "ServerStatus"
                + infix + "[\"A\", \"B\"]" + suffix);
        result = validator.validateUniqueness().apply(schema3);
        assertEquals(0, result.count());
        result = validator.validateUniqueness().apply(schema3);
        assertEquals(0, result.count());

        result = validator.validateUniqueness().apply(schemaAlt);
        assertEquals(1, result.count());
    }
}
