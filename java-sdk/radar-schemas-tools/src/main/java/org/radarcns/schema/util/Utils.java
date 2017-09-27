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

package org.radarcns.schema.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * TODO.
 */
public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final String GRADLE_PROPERTIES = "exchange.properties";
    private static final String GROUP_PROPERTY = "project.group";
    private static String projectGroup;

    private Utils() {
        //Static class
    }

    /**
     * TODO.
     * @return TODO
     */
    public static synchronized String getProjectGroup() {
        if (projectGroup == null) {
            Properties prop = new Properties();
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            try (InputStream in = loader.getResourceAsStream(GRADLE_PROPERTIES)) {
                if (in == null) {
                    projectGroup = "org.radarcns";
                    logger.debug("Project group not specified. Using \"{}\".", projectGroup);
                } else {
                    prop.load(in);
                    projectGroup = prop.getProperty(GROUP_PROPERTY);
                    if (projectGroup == null) {
                        projectGroup = "org.radarcns";
                        logger.debug("Project group not specified. Using \"{}\".", projectGroup);
                    }
                }
            } catch (IOException exc) {
                throw new IllegalStateException(GROUP_PROPERTY
                    + " cannot be extracted from " + GRADLE_PROPERTIES, exc);
            }
        }

        return projectGroup;
    }

    public static String expandClass(String classShorthand) {
        if (classShorthand == null || classShorthand.isEmpty()) {
            return null;
        } else if (classShorthand.charAt(0) == '.') {
            return getProjectGroup() + classShorthand;
        } else {
            return classShorthand;
        }
    }

    public static String toSnakeCase(String value) {
        char[] fileName = value.toCharArray();

        StringBuilder builder = new StringBuilder(fileName.length);

        boolean nextIsUpperCase = true;
        for (char c : fileName) {
            switch (c) {
                case '_':
                    nextIsUpperCase = true;
                    break;
                case '.':
                    return builder.toString();
                default:
                    if (nextIsUpperCase) {
                        builder.append(String.valueOf(c).toUpperCase(Locale.ENGLISH));
                        nextIsUpperCase = false;
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }

        return builder.toString();
    }

    public static <T, R> Function<T, Stream<R>> applyOrEmpty(ThrowingFunction<T, Stream<R>> func) {
        return t -> {
            try {
                return func.apply(t);
            } catch (Exception ex) {
                logger.error("Failed to apply function, returning empty.", ex);
                return Stream.empty();
            }
        };
    }

    public static <T> Predicate<T> testOrFalse(ThrowingPredicate<T> test) {
        return t -> {
            try {
                return test.test(t);
            } catch (Exception ex) {
                logger.error("Failed to test predicate, returning false.", ex);
                return false;
            }
        };
    }

    @FunctionalInterface
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public interface ThrowingFunction<T, R> {
        R apply(T value) throws Exception;
    }

    @FunctionalInterface
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public interface ThrowingPredicate<T> {
        boolean test(T value) throws Exception;
    }
}
