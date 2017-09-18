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

package org.radarcns.schema.validation.rules;

import org.radarcns.schema.specification.Topic;
import org.radarcns.schema.util.Utils;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationSupport;

import java.util.Collection;
import java.util.Objects;

import static org.radarcns.schema.validation.ValidationSupport.isValidClass;
import static org.radarcns.schema.validation.ValidationSupport.isValidTopic;
import static org.radarcns.schema.validation.rules.Validator.validate;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public final class TopicRoles {
    public static final String TOPIC_KEY_PACKAGE = Utils.getProjectGroup()
            + ValidationSupport.Package.KAFKA_KEY.getName();

    /** Messages. */
    private static final String KEY = "Kafka key class is invalid, cannot be null and must be a"
            + " valid AVRO schema located at " + TOPIC_KEY_PACKAGE + ".";
    private static final String TOPIC = "Topic name is invalid.";
    private static final String VALUE = "Kafka value class is invalid. It cannot be null and must"
            + " be a valida AVRO schema located at";

    private TopicRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Topic> validateKey() {
        return validateNonNull(Topic::getInputKey,
                key -> key.startsWith(TOPIC_KEY_PACKAGE) && isValidClass(key), KEY);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Topic> validateValue(ValidationSupport.Package packageName) {
        Objects.requireNonNull(packageName);
        return validateNonNull(Topic::getInputValue,
                value -> value.startsWith(Utils.getProjectGroup() + packageName.getName())
                        && isValidClass(value), VALUE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<String> validateTopic() {
        return validate(ValidationSupport::isValidTopic,
                topic -> TOPIC + ValidationSupport.isValidTopicVerbose(topic));
    }

    static Validator<? extends Collection<String>> validateTopics() {
        return topics -> topics.stream()
                    .filter(topic -> !isValidTopic(topic))
                    .map(topic -> new ValidationException(
                            TOPIC + ValidationSupport.isValidTopicVerbose(topic)));
    }
}
