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

import static org.radarcns.validator.SchemaValidationRoles.UNKNOWN;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

/**
 * TODO.
 */
public final class ValidationSupport {

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
     * @param scope TODO
     * @return TODO
     */
    public static String getNamespace(Path schemaPath, Scope scope) {
        String expected = getProjectGroup() + '.' + scope.getName();

        // add subfolder of root to namespace
        Path rootPath = scope.getCommonsPath();
        Path relativePath = rootPath.relativize(schemaPath);
        if (relativePath.getNameCount() > 1) {
            expected = expected + '.' + relativePath.getName(0);
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

        char[] fileName = path.getFileName().toString().toCharArray();

        StringBuilder recordName = new StringBuilder(fileName.length);

        boolean nextIsUpperCase = true;
        for (char c : fileName) {
            switch (c) {
                case '_':
                    nextIsUpperCase = true;
                    break;
                case '.':
                    return recordName.toString();
                default:
                    if (nextIsUpperCase) {
                        recordName.append(String.valueOf(c).toUpperCase(Locale.ENGLISH));
                        nextIsUpperCase = false;
                    } else {
                        recordName.append(c);
                    }
                    break;
            }
        }

        return recordName.toString();
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
            .map(Field::schema)
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

        for (Field field : input.getFields()) {
            boolean flag = true;
            switch (field.schema().getType()) {
                case RECORD:
                    flag = validateDefault(field.schema());
                    break;
                case UNION:
                    flag = field.defaultVal().equals(JsonProperties.NULL_VALUE);
                    break;
                case ENUM:
                    flag = field.schema().getEnumSymbols().contains(UNKNOWN)
                            && field.defaultVal().equals(UNKNOWN);
                    break;
                default: break;
            }

            if (!flag) {
                return false;
            }
        }

        return true;
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


    /**
     * TODO.
     * @param file TODO.
     * @return TODO.
     */
    public static boolean matchesExtension(Path file, String extension) {
        return file.toString().toLowerCase(Locale.ENGLISH)
                .endsWith("." + extension.toLowerCase(Locale.ENGLISH));
    }
}
