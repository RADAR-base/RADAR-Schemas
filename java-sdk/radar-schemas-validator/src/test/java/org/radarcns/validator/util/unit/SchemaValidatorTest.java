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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import junit.framework.TestCase;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;
import org.radarcns.validator.StructureValidator.NameFolder;
import org.radarcns.validator.util.SchemaValidator;
import org.radarcns.validator.util.ValidationResult;
import org.slf4j.LoggerFactory;

public class SchemaValidatorTest {

    @Test
    public void fileNameTest() {
        assertEquals("Questionnaire",
                SchemaValidator.getRecordName("questionnaire.avsc"));
        assertEquals("ApplicationExternalTime",
                SchemaValidator.getRecordName("application_external_time.avsc"));
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
                    .builder("org.radarcns.monitors.test")
                    .record("RecordTest")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(MONITOR, "test").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Namespace must be in the form \"org.radarcns.monitor.test\". "
                + "org.radarcns.monitors.test.RecordTest is invalid."), result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.tests")
                    .record("RecordTest")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateNameSpace(MONITOR, "test").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Namespace must be in the form \"org.radarcns.monitor.test\". "
                + "org.radarcns.monitor.tests.RecordTest is invalid."), result.getReason());
    }

    @Test
    public void recordNameTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.active.questionnaire")
                    .record("Questionnaire")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateRecordName("questionnaire.avsc").apply(schema);

        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder("org.radarcns.passive.empatica")
                    .record("EmpaticaE4Aceleration")
                    .fields()
                    .endRecord();

        result = SchemaValidator.validateRecordName("empatica_e4_acceleration.avsc").apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Record name must be the conversion of the .avsc file name in "
                + "UpperCamelCase. The expected value is EmpaticaE4Acceleration\". "
                + "org.radarcns.passive.empatica.EmpaticaE4Aceleration is invalid."),
                result.getReason());
    }

    @Test
    public void timeTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.test")
                    .record("RecordTest")
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidator.validateTime().apply(schema);

        assertFalse(result.isValid());

        assertEquals(Optional.of("Any schema representing collected data must have a \"time\" field"
          + " formatted in DOUBLE. org.radarcns.monitor.test.RecordTest is invalid."),
              result.getReason());

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.test")
                    .record("RecordTest")
                    .fields()
                    .requiredDouble("time")
                    .endRecord();

        result = SchemaValidator.validateTime().apply(schema);

        assertTrue(result.isValid());
    }

    @Test
    public void timeCompletedTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.test")
                    .record("RecordTest")
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidator.validateTimeCompleted().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any ACTIVE schema must have a \"timeCompleted\" field formatted "
              + "in DOUBLE. org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());

        result = SchemaValidator.validateNotTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                      .builder("org.radarcns.monitor.test")
                      .record("RecordTest")
                      .fields()
                      .requiredDouble("timeCompleted")
                      .endRecord();

        result = SchemaValidator.validateTimeCompleted().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidator.validateNotTimeCompleted().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("\"timeCompleted\" is allow only in ACTIVE schemas. "
              + "org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());
    }

    @Test
    public void timeReceivedTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.test")
                    .record("RecordTest")
                    .fields()
                    .requiredString("field")
                    .endRecord();

        result = SchemaValidator.validateTimeReceived().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Any PASSIVE schema must have a \"timeReceived\" field formatted "
            + "in DOUBLE. org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());

        result = SchemaValidator.validateNotTimeReceived().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
                    .builder("org.radarcns.monitor.test")
                    .record("RecordTest")
                    .fields()
                    .requiredDouble("timeReceived")
                    .endRecord();

        result = SchemaValidator.validateTimeReceived().apply(schema);
        assertTrue(result.isValid());

        result = SchemaValidator.validateNotTimeReceived().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("\"timeReceived\" is allow only in PASSIVE schemas. "
              + "org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());
    }

    @Test
    public void testRegex() {
        assertTrue("x".matches(FIELD_NAME_REGEX));
        assertTrue("time".matches(FIELD_NAME_REGEX));
        assertTrue("subjectId".matches(FIELD_NAME_REGEX));
        assertTrue("listOfSeveralThings".matches(FIELD_NAME_REGEX));
        assertFalse("Time".matches(FIELD_NAME_REGEX));
        assertFalse("E4Heart".matches(FIELD_NAME_REGEX));
    }

    @Test
    public void fieldNameTest() {
        Schema schema;
        ValidationResult result;

        schema = SchemaBuilder
                .builder("org.radarcns.monitor.test")
                .record("RecordTest")
                .fields()
                .requiredString("Formula1")
                .endRecord();

        result = SchemaValidator.validateFieldName().apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Field name does not respect lowerCamelCase name convention. "
              + "org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());

        schema = SchemaBuilder
          .builder("org.radarcns.monitor.test")
          .record("RecordTest")
          .fields()
          .requiredString("Formula1")
          .requiredString("time")
          .endRecord();

        result = SchemaValidator.validateFieldName(Collections.singleton("time")).apply(schema);
        assertFalse(result.isValid());
        assertEquals(Optional.of("Field name does not respect lowerCamelCase name convention. "
              + "org.radarcns.monitor.test.RecordTest is invalid."), result.getReason());

        schema = SchemaBuilder
              .builder("org.radarcns.monitor.test")
              .record("RecordTest")
              .fields()
              .requiredDouble("timeReceived")
              .endRecord();

        result = SchemaValidator.validateFieldName().apply(schema);
        assertTrue(result.isValid());

        schema = SchemaBuilder
              .builder("org.radarcns.monitor.test")
              .record("RecordTest")
              .fields()
              .requiredString("Formula1")
              .requiredString("time")
              .endRecord();

        result = SchemaValidator.validateFieldName(Collections.singleton("Formula1")).apply(schema);
        assertTrue(result.isValid());
    }

}
