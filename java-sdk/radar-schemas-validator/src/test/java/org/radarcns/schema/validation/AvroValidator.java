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
import org.junit.Before;
import org.junit.Test;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;
import static org.radarcns.schema.validation.SchemaRepository.COMMONS_PATH;

public final class AvroValidator {
    public static final String AVRO_EXTENSION = "avsc";
    private ExcludeConfig config;

    @Before
    public void setUp() {
        config = ExcludeConfig.load();
    }

    @Test
    public void active() throws IOException {
        analyseFiles(Scope.ACTIVE);
    }

    @Test
    public void monitor() throws IOException {
        analyseFiles(Scope.MONITOR);
    }

    @Test
    public void passive() throws IOException {
        analyseFiles(Scope.PASSIVE);
    }

    @Test
    public void kafka() throws IOException {
        analyseFiles(Scope.KAFKA);
    }

    @Test
    public void catalogue() throws IOException {
        analyseFiles(Scope.CATALOGUE);
    }

    /**
     * TODO.
     * @param scope TODO.
     * @throws IOException TODO.
     */
    public void analyseFiles(Scope scope)
            throws IOException {
        Parser parser = new Parser();
        String errors = Files.walk(scope.getPath(COMMONS_PATH))
                .filter(Files::isRegularFile)
                .filter(p -> !config.skipFile(p))
                .map(p -> {
                    if (!isAvscFile(p)) {
                        return new InvalidResult(scope.getLower() + " should contain only "
                                + AVRO_EXTENSION + " files. " + p.toAbsolutePath()
                                + " is invalid.");
                    }

                    try {
                        Schema schema = parser.parse(p.toFile());

                        if (config.contains(schema)) {
                            return SchemaValidator.validate(schema, p, scope,
                                    config.isNameRecordEnable(schema),
                                    config.skippedNameFieldCheck(schema));
                        } else {
                            return SchemaValidator.validate(schema, p, scope);
                        }
                    } catch (IOException e) {
                        return new InvalidResult("Cannot parse file: " + e);
                    }
                })
                .filter(r -> !r.isValid())
                .map(r -> "\nValidation FAILED:\n" + r.getReason().orElse("") + "\n")
                .collect(Collectors.joining());

        if (!errors.isEmpty()) {
            fail(errors);
        }
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isAvscFile(Path file) {
        return ValidationSupport.matchesExtension(file, AVRO_EXTENSION);
    }
}
