package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.GenericRoles.Package.AGGREGATOR;
import static org.radarcns.specifications.validator.GenericRoles.Package.KAFKA_KEY;
import static org.radarcns.specifications.validator.ValidationSupport.isValidClass;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopic;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopicVerbose;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopicsVerbose;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.radarcns.catalogue.Unit;
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
interface GenericRoles<T> extends Function<T, ValidationResult> {

    /** Package names. */
    enum Package {
        AGGREGATOR(".kafka.aggregator"),
        KAFKA_KEY(".kafka.key"),
        MONITOR(".monitor"),
        QUESTIONNAIRE(".active.questionnaire"),;

        private final String name;

        Package(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /** Messages. */
    enum GeneralInfo implements Message {
        AGGREGATOR("Kafka aggregator class is invalid, cannot be null and must be a valid AVRO "
            .concat("schema located at").concat(Package.AGGREGATOR.getName()).concat(".")),
        DOCUMENTATION("Documentation should not be null and should be ended by a dot."),
        KEY("Kafka key class is invalid, cannot be null and must be a valid AVRO schema located at "
                .concat(Utils.getProjectGroup()).concat(KAFKA_KEY.getName()).concat(".")),
        NAME("Name cannot be null"),
        SAMPLE_RATE("Sample rate cannot be null."),
        TOPIC("Topic name is invalid."),
        UNIT("Unit cannot be null or equal to ".concat(Unit.UNKNOWN.name()).concat(".")),
        VALUE("Kafka value class is invalid. It cannot be null and must be a valida AVRO schema"
            + "located at");

        private final String message;

        GeneralInfo(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getMessage(String info) {
            return message.concat(" ").concat(info);
        }
    }

    /**
     * TODO.
     * @param name
     * @return TODO
     */
    static GenericRoles validateName(String name) {
        return object -> Objects.nonNull(name) ? valid() : invalid(GeneralInfo.NAME.getMessage());
    }

    /**
     * TODO.
     * @param key
     * @return TODO
     */
    static GenericRoles validateKey(String key) {
        return object -> Objects.nonNull(key)
                        && isValidClass(key)
                        && key.startsWith(Utils.getProjectGroup().concat(KAFKA_KEY.getName())) ?
                valid() : invalid(GeneralInfo.KEY.getMessage());
    }

    /**
     * TODO.
     * @param packageName
     * @param value
     * @return TODO
     */
    static GenericRoles validateValue(Package packageName, String value) {
        return object -> Objects.nonNull(packageName) && Objects.nonNull(value)
                        && isValidClass(value)
                        && value.startsWith(Utils.getProjectGroup().concat(packageName.getName())) ?
                valid() : invalid(GeneralInfo.VALUE.getMessage());
    }

    /**
     * TODO.
     * @param aggregator TODO
     * @param nullable TODO
     * @return TODO
     */
    static GenericRoles validateAggregator(String aggregator, boolean nullable) {
        return object -> nullable && Objects.isNull(aggregator)
                || Objects.nonNull(aggregator)
                && isValidClass(aggregator)
                && aggregator.startsWith(Utils.getProjectGroup().concat(AGGREGATOR.getName())) ?
                valid() : invalid(GeneralInfo.AGGREGATOR.getMessage());
    }

    /**
     * TODO.
     * @param topic TODO
     * @return TODO
     */
    static GenericRoles validateTopic(String topic) {
        return object -> Objects.nonNull(topic) && isValidTopic(topic) ? valid()
                : invalid(GeneralInfo.TOPIC.getMessage(isValidTopicVerbose(topic)));
    }

    /**
     * TODO.
     * @param topics TODO
     * @return TODO
     */
    static GenericRoles validateTopics(Collection<String> topics) {
        return object -> Objects.nonNull(topics)
                && !topics.isEmpty()
                && topics.stream().allMatch(topic -> isValidTopic(topic))
                ? valid() : invalid(GeneralInfo.TOPIC.getMessage(isValidTopicsVerbose(topics)));
    }

    /**
     * TODO.
     * @param doc TODO
     * @param nullable TODO
     * @return TODO
     */
    static GenericRoles validateDoc(String doc, boolean nullable) {
        return object -> nullable || Objects.nonNull(doc) && doc.endsWith(".") ?
                valid() : invalid(GeneralInfo.DOCUMENTATION.getMessage());
    }

    /**
     * TODO.
     * @param rate TODO
     * @return TODO
     */
    static GenericRoles validateSampleRate(double rate) {
        return object -> Objects.nonNull(rate) ? valid()
                : invalid(GeneralInfo.SAMPLE_RATE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles validateUnit(Unit unit) {
        return object-> Objects.nonNull(unit) && !unit.name().equals(Unit.UNKNOWN)?
            valid() : invalid(GeneralInfo.UNIT.getMessage());
    }

    /**
     *
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static GenericRoles validate(Predicate predicate, Message message) {
        return object -> predicate.test(object) ? valid()
                : invalid(message.getMessage());
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default GenericRoles and(GenericRoles<T> other) {
        return object -> {
            final ValidationResult result = this.apply((T)object);
            return result.isValid() ? other.apply((T) object) : result;
        };
    }
}
