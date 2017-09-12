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
import org.apache.avro.Schema.Parser;
import org.junit.Assert;
import org.radarcns.validator.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.radarcns.validator.ValidationSupport.matchesExtension;

public final class AvroValidator {
    public static final String AVRO_EXTENSION = "avsc";
    private final ExcludeConfig config;

    public AvroValidator(ExcludeConfig config) {
        this.config = config;
    }

    /**
     * TODO.
     * @param scope TODO.
     * @throws IOException TODO.
     */
    public void analyseFiles(Scope scope)
            throws IOException {
        Files.walk(scope.getCommonsPath())
                .filter(Files::isRegularFile)
                .filter(p -> !config.skipFile(p))
                .forEach(p -> {
                    Assert.assertTrue(scope.getName() + " should contain only "
                                    + AVRO_EXTENSION + " files. " + p.toAbsolutePath()
                                    + " is invalid.",
                            isAvscFile(p));

                    try {
                        Schema schema = new Parser().parse(p.toFile());

                        ValidationResult result;

                        if (config.contains(schema)) {
                            result = SchemaValidator.validate(schema, p, scope,
                                    config.isNameRecordEnable(schema),
                                    config.skippedNameFieldCheck(schema));
                        } else {
                            result = SchemaValidator.validate(schema, p, scope);
                        }

                        Assert.assertTrue(getMessage(result), result.isValid());
                    } catch (IOException e) {
                        throw new AssertionError("Cannot parse file", e);
                    }

                    //TODO add file layout validation
                });
    }

    private static String getMessage(ValidationResult result) {
        return result.getReason().orElse("");
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isAvscFile(Path file) {
        return matchesExtension(file, AVRO_EXTENSION);
    }
}
