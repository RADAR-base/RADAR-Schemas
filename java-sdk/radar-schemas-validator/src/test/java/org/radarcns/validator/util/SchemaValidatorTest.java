package org.radarcns.validator.util;

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
import static org.radarcns.validator.CatalogValidator.NameFolder.KAFKA;
import static org.radarcns.validator.CatalogValidator.NameFolder.MONITOR;
import static org.radarcns.validator.util.SchemaValidator.analyseCollision;
import static org.radarcns.validator.util.SchemaValidator.getPath;
import static org.radarcns.validator.util.SchemaValidator.resetCollision;
import static org.radarcns.validator.util.SchemaValidator.validate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaBuilder;
import org.junit.Test;

/**
 * TODO.
 */
public class SchemaValidatorTest {

    @Test
    public void testGetPath() {
        Path path = Paths.get("/Users/developer/Repositories/RADAR-Schemas/commons/"
                + "monitor/application/application_external_time.avsc");

        String expected = "/RADAR-Schemas/commons/monitor/application/"
                + "application_external_time.avsc";

        assertEquals(expected, getPath(path));
    }

    @Test
    public void testCollision() throws IOException {
        resetCollision();

        Parser parser = new Parser();

        String folder = "key";
        String windowed = "key_windowed_test.avsc";
        String measurement = "key_measurement_test.avsc";

        ValidationResult result = validate(
                parser.parse(getClass().getClassLoader().getResourceAsStream(windowed)),
                Paths.get("/Users/developer/Repositories/RADAR-Schemas/commons/kafka/key/"
                    + windowed), KAFKA, folder);

        assertTrue(result.isValid());

        result = validate(
                parser.parse(getClass().getClassLoader().getResourceAsStream(measurement)),
                Paths.get("/Users/developer/Repositories/RADAR-Schemas/commons/kafka/key/"
                    + measurement), KAFKA, folder);

        assertTrue(result.isValid());

        String expected = "\"sourceTestId\" appears in:\n"
                + "\t - org.radarcns.kafka.key.KeyMeasurementTest as STRING\n"
                + "\t - org.radarcns.kafka.key.KeyWindowedTest as STRING\n"
                + "In case they have different use-cases, "
                + "please modify the name field accordingly.\n"
                + "\"userTestId\" appears in:\n"
                + "\t - org.radarcns.kafka.key.KeyMeasurementTest as STRING\n"
                + "\t - org.radarcns.kafka.key.KeyWindowedTest as STRING\n"
                + "In case they have different use-cases, "
                + "please modify the name field accordingly.\n";

        String actual = analyseCollision().toString();

        resetCollision();

        assertEquals(expected, actual);
    }

    @Test
    public void testEnumerator() {
        Path schemaPath =  Paths.get("/Users/developer/Repositories/RADAR-Schemas/"
                + "commons/monitor/application/server_status.avsc");

        String subfolder = "application";

        String name = "org.radarcns.monitor.application.ServerStatus";
        String documentation = "Mock documentation.";

        Schema schema = SchemaBuilder
                .enumeration(name)
                .doc(documentation)
                .symbols("CONNECTED", "DISCONNECTED", "UNKNOWN");

        ValidationResult result = validate(schema, schemaPath, MONITOR, subfolder);

        assertTrue(result.isValid());

        schema = SchemaBuilder
                .enumeration(name)
                .doc(documentation)
                .symbols("CONNECTED", "DISCONNECTED", "un_known");

        result = validate(schema, schemaPath, MONITOR, subfolder);

        assertFalse(result.isValid());
    }

}
