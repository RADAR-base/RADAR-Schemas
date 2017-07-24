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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Parser;
import org.apache.avro.SchemaValidator;
import org.radarcns.validator.StructureValidator.NameFolder;
import org.radarcns.validator.util.SchemaValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AvroValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroValidator.class);

    public static final String AVRO_FORMAT = "avsc";
    public static final String YAML_FORMAT = "yml";

    public static final String PACKAGE = "org.radarcns";

    public static final String TIME = "time";
    public static final String TIME_RECEIVED = "timeReceived";
    public static final String TIME_COMPLETED = "timeCompleted";

    public static final String FIELD_NAME_REGEX = "^[a-z][a-zA-Z]*$";

    private static Map<String, String> skipName;

    static {
        skipName = new HashMap<>();
        skipName.put("spO2", "org.radarcns.passive.biovotion.BiovotionVSMSpO2");
        skipName.put("spO2Quality", "org.radarcns.passive.biovotion.BiovotionVSMSpO2");
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
        } else {
            assertEquals(packageName + "should contain only " + AVRO_FORMAT + " files",
                    AVRO_FORMAT, getExtension(file));

            Schema schema = new Parser().parse(file);

            if (parentName != null) {
                assertTrue(file.getName().startsWith(parentName));
                assertEquals(PACKAGE.concat(".").concat(packageName.getName()).concat(
                        ".").concat(parentName), schema.getNamespace());
            } else {
                assertEquals(PACKAGE.concat(".").concat(packageName.getName()),
                        schema.getNamespace());
            }

            switch (packageName) {
                case ACTIVE:
                    assertNotNull("Any " + NameFolder.ACTIVE.getName()
                                + " schema must have a " + TIME_COMPLETED + " field",
                                schema.getField(TIME_COMPLETED));
                    assertNull("To be compliant with the RADAR design, "
                                + TIME_RECEIVED + " field must be only in "
                                + NameFolder.PASSIVE.getName(), schema.getField(TIME_RECEIVED));
                    checkTimeField(schema, packageName);
                    break;
                case MONITOR:
                    checkTimeField(schema, packageName);
                    break;
                case PASSIVE:
                    assertNotNull("Any " + NameFolder.PASSIVE.getName()
                                + " schema must have a " + TIME_RECEIVED + " field",
                                schema.getField(TIME_RECEIVED));
                    assertNull("To be compliant with the RADAR design, "
                                + TIME_COMPLETED + " field must be only in "
                                + NameFolder.ACTIVE.getName(), schema.getField(TIME_COMPLETED));
                    checkTimeField(schema, packageName);
                    break;
                default: //Nothing to do
            }

            generalSchemaChecks(schema);
        }
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
     * @param schema TODO.
     * @param packageName TODO.
     */
    public static void checkTimeField(Schema schema, NameFolder packageName) {
        assertNotNull("Any " + packageName.getName() + " schema must have a " + TIME
                  + " field", schema.getField(TIME));
    }

    /**
     * TODO.
     * @param schema TODO.
     */
    public static void generalSchemaChecks(Schema schema) {
        for (Field field : schema.getFields()) {

            if (skipName.containsKey(field.name())
                    && skipName.get(field.name()).equalsIgnoreCase(schema.getFullName())) {
                LOGGER.warn("[Name field check] Skipping {} in {}",
                        field.name(), schema.getFullName());
                continue;
            }

            assertTrue(field.name() + " in " + schema.getFullName()
                    + " does not respect lowerCamelCase name convention",
                    field.name().matches(FIELD_NAME_REGEX));
        }
    }

}


//check if there is documentaion
//check if name folder is present in the name schema or viceversa
//check package name
//check contraction
//check if name contains value
//check name convention

//  - Try to avoid abbreviations in the field names and write out the field name instead.
//- There should be no need to add `value` at the end of a field name.
//- Enumerator items should be written in uppercase characters separated by underscores.
//- Add documentation (the `doc` property) to each schema and each field. The documentation should show in text what is being measured, how, and what units or ranges are applicable. Abbreviations and acronyms in the documentation should be written out.
//    - Prefer a categorical specification (an Avro enum) over a free string. This disambiguates the possible values for analysis.
//- Prefer a flat record over a hierarchical record. This simplifies the organization of the data downstream, for example, when mapping to CSV.
//    - Prefer written out fields to arrays. This simplifies the organization of the data downstream, for example, when mapping to CSV.
//    - Give each schema a proper namespace, preferably `org.radarcns.deviceproducer` fully in lowercase, without any numbers, uppercase letters or symbols (except `.`). For the Empatica E4, the producer is Empatica, so the namespace is `org.radarcns.empatica`. For generic types, like a phone, Android Wear device or Android application, the namespace could just be `org.radarcns.phone`, `org.radarcns.wear`, or `org.radarcns.application`.
//    - In the schema name, use upper camel case and name the device explicitly (for example, `EmpaticaE4Temperature`).
// warning on collision name
