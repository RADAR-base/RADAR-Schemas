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

import static org.radarbase.schema.util.SchemaUtils.getProjectGroup;
import static org.radarbase.schema.util.SchemaUtils.snakeToCamelCase;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.radarbase.schema.Scope;

/**
 * TODO.
 */
public final class ValidationHelper {
    public static final String COMMONS_PATH = "commons";
    public static final String SPECIFICATIONS_PATH = "specifications";

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

    // snake case
    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "[A-Za-z][a-z0-9-]*(_[A-Za-z0-9-]+)*");

    private ValidationHelper() {
        //Static class
    }

    /**
     * TODO.
     * @param scope TODO
     * @return TODO
     */
    public static String getNamespace(Path schemaRoot, Path schemaPath, Scope scope) {
        // add subfolder of root to namespace
        Path rootPath = scope.getPath(schemaRoot);
        if (rootPath == null) {
            throw new IllegalArgumentException("Scope " + scope + " does not have a commons path");
        }
        Path relativePath = rootPath.relativize(schemaPath);

        StringBuilder builder = new StringBuilder(50);
        builder.append(getProjectGroup()).append('.').append(scope.getLower());
        for (int i = 0; i < relativePath.getNameCount() - 1; i++) {
            builder.append('.').append(relativePath.getName(i));
        }
        return builder.toString();
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static String getRecordName(Path path) {
        Objects.requireNonNull(path);

        return snakeToCamelCase(path.getFileName().toString());
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
}
