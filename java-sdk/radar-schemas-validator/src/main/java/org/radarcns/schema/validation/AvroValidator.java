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

package org.radarcns.schema.validation;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.radarcns.schema.validation.SchemaRepository.COMMONS_PATH;

public final class AvroValidator {
    public static final String AVRO_EXTENSION = "avsc";
    private ExcludeConfig config;

    public AvroValidator(ExcludeConfig config) {
        this.config = config;
    }

    /**
     * TODO.
     * @param scope TODO.
     * @throws IOException TODO.
     */
    public Collection<ValidationException> analyseFiles(Scope scope)
            throws IOException {
        Parser parser = new Parser();
        return Files.walk(scope.getPath(COMMONS_PATH))
                .filter(Files::isRegularFile)
                .filter(p -> !config.skipFile(p))
                .flatMap(p -> {
                    if (!isAvscFile(p)) {
                        return Stream.of(new ValidationException(
                                p.toAbsolutePath() + " is invalid. " + scope.getLower()
                                + " should contain only " + AVRO_EXTENSION + " files."));
                    }

                    try {
                        Schema schema = parser.parse(p.toFile());

                        if (config.contains(schema)) {
                            return SchemaValidator.validate(schema, p, scope,
                                    config.isNameRecordEnable(schema),
                                    config.skippedNameFieldCheck(schema)).stream();
                        } else {
                            return SchemaValidator.validate(schema, p, scope).stream();
                        }
                    } catch (IOException e) {
                        return Stream.of(new ValidationException(
                                "Cannot parse file " + p.toAbsolutePath(), e));
                    }
                })
                .collect(Collectors.toList());
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
