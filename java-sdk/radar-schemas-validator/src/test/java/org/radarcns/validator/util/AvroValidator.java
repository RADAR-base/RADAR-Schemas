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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.radarcns.validator.CatalogValidator.NameFolder;
import org.radarcns.validator.config.SkipConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AvroValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroValidator.class);

    public static final String AVRO_FORMAT = "avsc";

    private AvroValidator() {
        //Static class
    }

    /**
     * TODO.
     * @param file TODO.
     * @param packageName TODO.
     * @param parentName TODO.
     * @throws IOException TODO.
     */
    public static void analiseFiles(File file, NameFolder packageName, String parentName)
            throws IOException {
        if (file.isDirectory()) {
            for (File son : file.listFiles()) {
                analiseFiles(son, packageName, file.getName());
            }
        } else if (SkipConfig.skipFile(file)) {
            LOGGER.debug("Skipping {}", file.getAbsolutePath());
        } else {
            assertTrue(packageName.getName().concat(" should contain only ")
                                            .concat(AVRO_FORMAT).concat(" files. ")
                                            .concat(file.getAbsolutePath()).concat(" is invalid."),
                        isAvscFile(file));

            Schema schema = new Parser().parse(file);

            ValidationResult result;

            if (SkipConfig.contains(schema)) {
                result = SchemaValidator.validate(schema, file.toPath(), packageName, parentName,
                        SkipConfig.isNameRecordEnable(schema),
                        SkipConfig.skippedNameFieldCheck(schema));
            } else {
                result = SchemaValidator.validate(schema, file.toPath(), packageName, parentName);
            }

            assertTrue(getMessage(result), result.isValid());

            //TODO add file layout validation
        }
    }

    private static String getMessage(ValidationResult result) {
        StringBuilder messageBuilder = new StringBuilder(200);
        result.getReason().ifPresent(s -> messageBuilder.append(s));
        return messageBuilder.toString();
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isAvscFile(File file) {
        return AVRO_FORMAT.equals(getExtension(file));
    }

    /**
     * TODO.
     * @param file TODO.
     * @return TODO.
     */
    public static String getExtension(File file) {
        String extension = "";
        int index = file.getName().lastIndexOf('.');
        if (index > 0) {
            extension = file.getName().substring(index + 1);
        }

        return extension;
    }

    /**
     * TODO.
     * @param reset TODO
     */
    public static void analyseNamingCollision(boolean reset) {
        String message = SchemaValidator.analyseCollision().toString();
        if (!"".equalsIgnoreCase(message)) {
            LOGGER.warn("Different schemas have fields with same names:\n{}", message);
        }

        if (reset) {
            SchemaValidator.resetCollision();
        }
    }

}
