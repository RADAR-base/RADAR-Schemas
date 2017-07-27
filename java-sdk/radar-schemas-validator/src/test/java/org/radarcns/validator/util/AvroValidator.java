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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.radarcns.validator.CatalogValidator.NameFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AvroValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroValidator.class);

    public static final String AVRO_FORMAT = "avsc";
    public static final String README_FILE = "README.md";

    private static final String WILD_CARD = ".*";

    private static Map<String, SkipConfig> skip;

    static {
        skip = new HashMap<>();
        skip.put("org.radarcns.passive.biovotion.BiovotionVSMSpO2",
                new SkipConfig(true, "spO2", "spO2Quality"));
        skip.put("org.radarcns.passive.biovotion.*", new SkipConfig(true));
        skip.put("org.radarcns.passive.empatica.EmpaticaE4InterBeatInterval",
                new SkipConfig("interBeatInterval"));
    }

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
        } else if (!file.getName().equalsIgnoreCase(README_FILE)) {
            assertEquals(packageName + "should contain only " + AVRO_FORMAT + " files",
                    AVRO_FORMAT, getExtension(file));

            Schema schema = new Parser().parse(file);

            ValidationResult result;

            SkipConfig skipConfig = getSkipConfig(schema);

            if (Objects.isNull(skipConfig)) {
                result = SchemaValidator.validate(schema, file.toPath(), packageName, parentName);
            } else {
                result = SchemaValidator.validate(schema, file.toPath(), packageName, parentName,
                    skipConfig.isNameRecord(), skipConfig.getFields());
            }

            StringBuilder messageBuilder = new StringBuilder(200);
            result.getReason().ifPresent(s -> messageBuilder.append(s));

            assertTrue(messageBuilder.toString(), result.isValid());
        }
    }

    private static SkipConfig getSkipConfig(Schema schema) {
        SkipConfig skipConfig = skip.get(schema.getFullName());
        return Objects.isNull(skipConfig)
                ? skip.get(schema.getNamespace().concat(WILD_CARD)) : skipConfig;
    }

    /**
     * TODO.
     * @param file TODO.
     * @return TODO.
     */
    private static String getExtension(File file) {
        String extension = "";
        int index = file.getName().lastIndexOf('.');
        if (index > 0) {
            extension = file.getName().substring(index + 1);
        }

        return extension;
    }

    /**
     * TODO.
     */
    public static void analyseNamingCollsion() {
        String message = SchemaValidator.analyseCollision().toString();
        if ("".equalsIgnoreCase(message)) {
            LOGGER.warn(message);
        }
    }

}
