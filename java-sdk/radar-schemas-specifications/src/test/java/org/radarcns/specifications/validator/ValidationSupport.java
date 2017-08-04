package org.radarcns.specifications.validator;

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

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.apache.kafka.common.errors.InvalidTopicException;
import scala.MatchError;

/**
 * TODO.
 */
public final class ValidationSupport {

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
     * @param className TODO.
     * @return TODO.
     */
    public static boolean isValidClass(String className){
        try {
            Class.forName(className);
            return true;
        } catch( ClassNotFoundException e ) {
            return false;
        }
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static boolean isValidTopic(String topicName) {
        try {
            kafka.common.Topic.validate(topicName);
            return true;
        } catch (InvalidTopicException | MatchError exc) {
            return false;
        }
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static String isValidTopicVerbose(String topicName) {
        if (topicName == null) {
            return ". Topic is null.";
        }
        try {
            kafka.common.Topic.validate(topicName);
            return "";
        } catch (InvalidTopicException | MatchError exc) {
            return exc.toString();
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
                    reason.append(", ".concat(temp));
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
     * @param file TODO
     * @return TODO
     */
    public static String getMessage(File file, ValidationResult result) {
        if (result.isValid()) {
            return "";
        }

        return result.getReason().get().concat(" ").concat(
                file.getAbsolutePath()).concat(" is invalid.");
    }

    /**
     * TODO.
     * @param file TODO
     * @param extension TODO
     * @return TODO
     */
    public static String removeExtension(File file, String extension) {
        String value = file.getName();
        if (value.endsWith(extension)) {
            value = value.substring(0, value.length() - extension.length());
        }
        return value;
    }

}
