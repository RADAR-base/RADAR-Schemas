package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.ValidationSupport.Package;
import static org.radarcns.specifications.validator.ValidationSupport.isValidClass;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopic;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopicVerbose;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopicsVerbose;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import org.radarcns.specifications.source.Topic;
import org.radarcns.specifications.util.Utils;

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

/**
 * TODO.
 */
interface TopicRoles extends GenericRoles<Topic> {

    /** Messages. */
    enum TopicInfo implements Message {
        KEY("Kafka key class is invalid, cannot be null and must be a valid AVRO schema located at "
                .concat(Utils.getProjectGroup()).concat(Package.KAFKA_KEY.getName()).concat(".")),
        TOPIC("Topic name is invalid."),
        VALUE("Kafka value class is invalid. It cannot be null and must be a valida AVRO".concat(
                "schema located at"));

        private final String message;

        TopicInfo(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getMessage(String info) {
            return message.concat(" ").concat(info);
        }
    }

    //public String getInputTopic()
    //public boolean hasAggregator()
    //public String getAggregator()
    //public Set<TopicMetadata> getOutput()
    //public Set<String> getTopicNames()

    /**
     * TODO.
     * @return TODO
     */
    static TopicRoles validateKey() {
        return topic -> Objects.nonNull(topic.getInputKey())
                        && topic.getInputKey().startsWith(Utils.getProjectGroup().concat(
                                Package.KAFKA_KEY.getName()))
                        && isValidClass(topic.getInputKey()) ?
                valid() : invalid(TopicInfo.KEY.getMessage());
    }

    /**
     * TODO.
     * @param packageName
     * @return TODO
     */
    static TopicRoles validateValue(Package packageName) {
        return topic -> Objects.nonNull(packageName) && Objects.nonNull(topic.getInputValue())
                        && topic.getInputValue().startsWith(Utils.getProjectGroup().concat(
                                packageName.getName()))
                        && isValidClass(topic.getInputValue()) ?
                valid() : invalid(TopicInfo.VALUE.getMessage());
    }

    /**
     * TODO.
     * @param topic TODO
     * @return TODO
     */
    static TopicRoles validateTopic(String topic) {
        return object -> Objects.nonNull(topic) && isValidTopic(topic) ? valid()
                : invalid(TopicInfo.TOPIC.getMessage(isValidTopicVerbose(topic)));
    }

    /**
     * TODO.
     * @param topics TODO
     * @return TODO
     */
    static TopicRoles validateTopicNames(Collection<String> topics) {
        return object -> Objects.nonNull(topics)
                && !topics.isEmpty()
                && topics.stream().allMatch(topic -> isValidTopic(topic))
                ? valid() : invalid(TopicInfo.TOPIC.getMessage(isValidTopicsVerbose(topics)));
    }

    /**
     *
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static TopicRoles validate(Predicate predicate, Message message) {
        return object -> predicate.test(object) ? valid()
                : invalid(message.getMessage());
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default TopicRoles and(TopicRoles other) {
        return object -> {
            final ValidationResult result = this.apply(object);
            return result.isValid() ? other.apply(object) : result;
        };
    }
}
