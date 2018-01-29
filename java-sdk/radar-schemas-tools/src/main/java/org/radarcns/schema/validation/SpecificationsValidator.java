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

import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.radarcns.schema.SchemaRepository.SPECIFICATIONS_PATH;

/**
 * Validates RADAR-Schemas specifications.
 */
public class SpecificationsValidator {
    public static final String YML_EXTENSION = "yml";
    private final ExcludeConfig config;
    private final Path root;

    /**
     * Specifications validator for given RADAR-Schemas directory.
     * @param root RADAR-Schemas directory.
     * @param config configuration to exclude certain schemas or fields from validation.
     */
    public SpecificationsValidator(Path root, ExcludeConfig config) {
        this.root = root;
        this.config = config;
    }

    /** Check that all files in the specifications directory are YAML files. */
    public boolean specificationsAreYmlFiles(Scope scope) throws IOException {
        return Files.walk(scope.getPath(root.resolve(SPECIFICATIONS_PATH)))
                    .filter(Files::isRegularFile)
                    .filter(p -> !config.skipFile(p))
                    .allMatch(SpecificationsValidator::isYmlFile);
    }

    private static boolean isYmlFile(Path path) {
        return ValidationSupport.matchesExtension(path, YML_EXTENSION);
    }
}
