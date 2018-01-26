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

    /**
     * Expand a class name with the group name if it starts with a dot.
     * @param classShorthand class name, possibly starting with a dot as a shorthand.
     * @return class name or {@code null} if null or empty.
     */
    public static String expandClass(String classShorthand) {
        if (classShorthand == null || classShorthand.isEmpty()) {
            return null;
        } else if (classShorthand.charAt(0) == '.') {
            return getProjectGroup() + classShorthand;
        } else {
            return classShorthand;
        }
    }

    /**
     * Converts given file name from snake_case to CamelCase. This will cause underscores to be
     * removed, and the next character to be uppercase. This only converts the value up to the
     * first dot encountered.
     * @param value file name in snake_case
     * @return main part of file name in CamelCase.
     */
    public static String snakeToCamelCase(String value) {
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

    /** Apply a throwing function, and if it throws, log it and let it return an empty Stream. */
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

    /** Test a throwing predicate, and if it throws, log it and let it return false. */
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

    /**
     * Function that may throw an exception.
     * @param <T> type of value taken.
     * @param <R> type of value returned.
     */
    @FunctionalInterface
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public interface ThrowingFunction<T, R> {
        /**
         * Apply containing function.
         * @param value value to apply function to.
         * @return result of the function.
         * @throws Exception if the function fails.
         */
        R apply(T value) throws Exception;
    }

    /**
     * Predicate that may throw an exception.
     * @param <T> type of value taken.
     */
    @FunctionalInterface
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public interface ThrowingPredicate<T> {
        /**
         * Test containing predicate.
         * @param value value to test predicate for.
         * @return whether the predicate tests true.
         * @throws Exception if the predicate fails to evaluate.
         */
        boolean test(T value) throws Exception;
    }
}
