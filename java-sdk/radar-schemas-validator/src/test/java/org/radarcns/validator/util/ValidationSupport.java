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

import static org.radarcns.validator.util.SchemaValidationRoles.UNKNOWN;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.radarcns.validator.SchemaCatalogValidator.NameFolder;

/**
 * TODO.
 */
final class ValidationSupport {

    private static final String GRADLE_PROPERTIES = "gradle.properties";
    private static final String PROPERTY_VALUE = "project.group";
    private static String projectGroup;

    static final ValidationResult VALID = new ValidationResult() {
        public boolean isValid() {
            return true;
        }

        public Optional<String> getReason() {
            return Optional.empty();
        }
    };


    private ValidationSupport() {
        //Static class
    }

    static ValidationResult getValid() {
        return VALID;
    }

    /**
     * TODO.
     * @return TODO
     */
    public static String getProjectGroup() {
        if (Objects.isNull(projectGroup)) {
            try {
                Properties prop = new Properties();
                prop.load(ValidationSupport.class.getClassLoader().getResourceAsStream(
                        GRADLE_PROPERTIES));
                projectGroup = prop.getProperty(PROPERTY_VALUE);
            } catch (IOException exc) {
                throw new IllegalStateException(PROPERTY_VALUE.concat(
                        " cannot be extracted from ").concat(GRADLE_PROPERTIES), exc);
            }
        }

        return projectGroup;
    }

    /**
     * TODO.
     * @param rootFolder TODO
     * @param subFolder TODO
     * @return TODO
     */
    public static String getNamespace(NameFolder rootFolder, String subFolder) {
        String expected = getProjectGroup().concat(".").concat(rootFolder.getName());

        if (!Objects.isNull(subFolder)) {
            expected = expected.concat(".").concat(subFolder);
        }

        return expected;
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static String getRecordName(Path path) {
        Objects.requireNonNull(path);

        String recordName = "";

        String fileName = path.getFileName().toString();

        boolean start = true;
        for (int i = 0; i < fileName.length(); i++) {
            switch (fileName.charAt(i)) {
                case '_' :
                    start = true;
                    break;
                case '.' : return recordName;
                default:
                    recordName += start ? Character.toUpperCase(fileName.charAt(i))
                        : fileName.charAt(i);
                    start = false;
                    break;
            }
        }

        return recordName;
    }

    /**
     * TODO.
     * @param root TODO
     * @return TODO
     */
    public static List<String> extractEnumerationFields(Schema root) {
        if (root.getType().equals(Type.ENUM)) {
            return root.getEnumSymbols();
        }

        final List<String> symbols = new LinkedList<>();
        root.getFields().stream()
            .filter(field -> field.schema().getType().equals(Type.ENUM))
            .map(field -> field.schema())
            .forEach(schema -> symbols.addAll(schema.getEnumSymbols()));

        return symbols;
    }

    /**
     * TODO.
     * @param input TODO
     * @return TODO
     * @throws IllegalArgumentException TODO
     */
    //TODO analyse schemas and improve
    public static boolean validateDefault(Schema input) {
        if (!input.getType().equals(Type.RECORD)) {
            throw new IllegalArgumentException("Function can be applied only to avro RECORD.");
        }

        boolean flag = true;

        for (Field field : input.getFields()) {
            switch (field.schema().getType()) {
                case RECORD:
                    flag = flag && validateDefault(field.schema());
                    break;
                case UNION:
                    flag = flag && field.defaultVal().equals(JsonProperties.NULL_VALUE);
                    break;
                case ENUM:
                    flag = flag && field.schema().getEnumSymbols().contains(UNKNOWN)
                            && field.defaultVal().equals(UNKNOWN);
                    break;
                default: break;
            }

            if (!flag) {
                return flag;
            }
        }

        return flag;
    }

    /**
     * TODO.
     * @param defaultVal TODO
     * @param type TODO
     * @return TODO
     */
    //TODO analyse schemas and redesign
    /*private static boolean basicValidateDefault(Object defaultVal, Type type) {
        switch (type) {
            case INT:
                return defaultVal.equals(Integer.MIN_VALUE) || defaultVal.equals(Integer.MAX_VALUE);
            case LONG:
                return defaultVal.equals(Long.MIN_VALUE) || defaultVal.equals(Long.MAX_VALUE);
            case DOUBLE:
                return defaultVal.equals("NaN");
            case FLOAT:
                return defaultVal.equals("NaN");
            case BOOLEAN:
                return defaultVal instanceof Boolean;
            case BYTES:
                //TODO check if there is better way
                return defaultVal instanceof String;
            default:
                return defaultVal.equals(JsonProperties.NULL_VALUE);
        }
    }*/

}
