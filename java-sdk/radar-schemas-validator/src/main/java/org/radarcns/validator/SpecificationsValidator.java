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

import org.radarcns.validator.config.ExcludeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.radarcns.validator.ValidationSupport.matchesExtension;

/**
 * TODO.
 */
public class SpecificationsValidator {
    public static final String YML_EXTENSION = "yml";
    private final ExcludeConfig config;

    public SpecificationsValidator(ExcludeConfig config) {
        this.config = config;
    }

    public boolean specificationsAreYmlFiles(Scope scope) throws IOException {
        return Files.walk(scope.getSpecificationsPath())
                    .filter(Files::isRegularFile)
                    .filter(p -> !config.skipFile(p))
                    .allMatch(SpecificationsValidator::isYmlFile);
    }

    private static boolean isYmlFile(Path path) {
        return matchesExtension(path, YML_EXTENSION);
    }
}
