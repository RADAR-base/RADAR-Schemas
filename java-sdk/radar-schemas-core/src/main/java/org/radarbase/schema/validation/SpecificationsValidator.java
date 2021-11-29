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

package org.radarbase.schema.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarbase.schema.Scope;
import org.radarbase.schema.validation.config.ExcludeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH;

/**
 * Validates RADAR-Schemas specifications.
 */
public class SpecificationsValidator {
    private static final Logger logger = LoggerFactory.getLogger(SpecificationsValidator.class);

    public static final String YML_EXTENSION = "yml";
    private final ExcludeConfig config;
    private final Path specificationsRoot;
    private final ObjectMapper mapper;

    /**
     * Specifications validator for given RADAR-Schemas directory.
     * @param root RADAR-Schemas directory.
     * @param config configuration to exclude certain schemas or fields from validation.
     */
    public SpecificationsValidator(Path root, ExcludeConfig config) {
        this.specificationsRoot = root.resolve(SPECIFICATIONS_PATH);
        this.config = config;
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    /** Check that all files in the specifications directory are YAML files. */
    public boolean specificationsAreYmlFiles(Scope scope) throws IOException {
        Path baseFolder = scope.getPath(specificationsRoot);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return false;
        }

        return Files.walk(baseFolder)
                    .filter(p -> Files.isRegularFile(p) && !config.skipFile(p))
                    .allMatch(SpecificationsValidator::isYmlFile);
    }

    public <T> boolean checkSpecificationParsing(Scope scope, Class<T> clazz) throws IOException {
        Path baseFolder = scope.getPath(specificationsRoot);
        if (baseFolder == null) {
            logger.info(scope + " sources folder not present");
            return false;
        }

        return Files.walk(baseFolder)
                .filter(Files::isRegularFile)
                .allMatch(f -> {
                    try {
                        mapper.readerFor(clazz).<T>readValue(f.toFile());
                        return true;
                    } catch (IOException ex) {
                        logger.error("Failed to load configuration {}: {}", f, ex.toString());
                        return false;
                    }
                });
    }

    private static boolean isYmlFile(Path path) {
        return ValidationHelper.matchesExtension(path, YML_EXTENSION);
    }
}
