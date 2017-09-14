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

import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.radarcns.schema.Scope;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.radarcns.schema.specification.Utils.toSnakeCase;
import static org.radarcns.schema.validation.SchemaRepository.COMMONS_PATH;
import static org.radarcns.schema.validation.roles.SchemaValidationRoles.UNKNOWN;

/**
 * TODO.
 */
public final class ValidationSupport {

    /** Package names. */
    public enum Package {
        AGGREGATOR(".kafka.aggregator"),
        BIOVOTION(".passive.biovotion"),
        EMPATICA(".passive.empatica"),
        KAFKA_KEY(".kafka.key"),
        MONITOR(".monitor"),
        PEBBLE(".passive.pebble"),
        QUESTIONNAIRE(".active.questionnaire");

        private final String name;

        Package(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String GRADLE_PROPERTIES = "gradle.properties";
    private static final String PROPERTY_VALUE = "project.group";
    private static String projectGroup;
    // snake case
    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "[A-Za-z][a-z0-9-]*(_[A-Za-z0-9-]+)*");

    private ValidationSupport() {
        //Static class
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
                throw new IllegalStateException(PROPERTY_VALUE
                        + " cannot be extracted from " + GRADLE_PROPERTIES, exc);
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
        // add subfolder of root to namespace
        Path rootPath = scope.getPath(COMMONS_PATH);
        if (rootPath == null) {
            throw new IllegalArgumentException("Scope " + scope + " does not have a commons path");
        }
        Path relativePath = rootPath.relativize(schemaPath);

        String expected = getProjectGroup() + '.' + scope.getLower();
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

        return toSnakeCase(path.getFileName().toString());
    }

    /**
     * TODO.
     * @param className TODO.
     * @return TODO.
     */
    public static boolean isValidClass(String className) {
        try {
            Class.forName(className).newInstance();
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return false;
        }
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
     * @param topicName TODO
     * @return TODO
     */
    public static boolean isValidTopic(String topicName) {
        return topicName != null && TOPIC_PATTERN.matcher(topicName).matches();
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static String isValidTopicVerbose(String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            return "Topic is not specified.";
        }
        Matcher matcher = TOPIC_PATTERN.matcher(topicName);
        if (matcher.find()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Kafka topic name is invalid (fix the string between brackets): \"");

            int start = matcher.start();
            int end = matcher.end();
            if (start > 0) {
                builder.append('[')
                        .append(topicName.substring(0, start))
                        .append(']');
            }
            builder.append(topicName.substring(start, end));
            while (matcher.find()) {
                start = matcher.start();
                builder.append('[')
                        .append(topicName.substring(end, start))
                        .append(']');
                end = matcher.end();
                builder.append(topicName.substring(start, end));
            }
            if (end < topicName.length()) {
                builder.append('[')
                        .append(topicName.substring(end, topicName.length()))
                        .append(']');
            }

            return builder
                    .append(". Use lower case alphanumeric strings with underscores.")
                    .toString();
        } else {
            return "Use lower case alphanumeric strings with underscores for Kafka topics";
        }
    }

    /**
     * TODO.
     * @param topicNames TODO
     * @return TODO
     */
    public static String isValidTopicsVerbose(Collection<String> topicNames) {
        Objects.requireNonNull(topicNames);

        StringBuilder reason = new StringBuilder(topicNames.size() * 100);
        boolean first = true;
        String temp;
        for (String value : topicNames) {
            temp = isValidTopicVerbose(value);
            if (!temp.isEmpty()) {
                if (first) {
                    reason.append(temp);
                    first = false;
                } else {
                    reason.append('\n');
                }
            }
        }

        if (first) {
            return "";
        }

        return reason.toString();
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
            boolean flag;
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
                default:
                    flag = field.defaultVal() == null;
                    break;
            }

            if (!flag) {
                return false;
            }
        }

        return true;
    }

    /**
     * TODO.
     * @param file TODO.
     * @return TODO.
     */
    public static boolean matchesExtension(Path file, String extension) {
        return file.toString().toLowerCase(Locale.ENGLISH)
                .endsWith("." + extension.toLowerCase(Locale.ENGLISH));
    }

    /**
     * TODO.
     * @param file TODO
     * @param extension TODO
     * @return TODO
     */
    public static boolean equalsFileName(String str, Path file, String extension) {
        return equalsFileName(file, extension).test(str);
    }


    /**
     * TODO.
     * @param file TODO
     * @param extension TODO
     * @return TODO
     */
    public static Predicate<String> equalsFileName(Path file, String extension) {
        return str -> {
            String fileName = file.getFileName().toString();
            if (fileName.endsWith(extension)) {
                fileName = fileName.substring(0, fileName.length() - extension.length());
            }

            return str.equalsIgnoreCase(fileName);
        };
    }

    public static boolean nonEmpty(Collection<?> c) {
        return c != null && !c.isEmpty();
    }
}
