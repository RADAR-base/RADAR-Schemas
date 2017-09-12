package org.radarcns.validator;

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
import org.apache.avro.SchemaBuilder;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.validator.Scope.MONITOR;
import static org.radarcns.validator.SchemaValidator.getPath;
import static org.radarcns.validator.SchemaValidator.validate;

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
    public void testEnumerator() {
        Path schemaPath =  SchemaRepository.PROJECT_ROOT.resolve(
                "commons/monitor/application/server_status.avsc");

        String name = "org.radarcns.monitor.application.ServerStatus";
        String documentation = "Mock documentation.";

        Schema schema = SchemaBuilder
                .enumeration(name)
                .doc(documentation)
                .symbols("CONNECTED", "DISCONNECTED", "UNKNOWN");

        ValidationResult result = validate(schema, schemaPath, MONITOR);

        assertTrue(result.isValid());

        schema = SchemaBuilder
                .enumeration(name)
                .doc(documentation)
                .symbols("CONNECTED", "DISCONNECTED", "un_known");

        result = validate(schema, schemaPath, MONITOR);

        assertFalse(result.isValid());
    }

}
