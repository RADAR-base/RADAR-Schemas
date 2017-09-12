package org.radarcns.schema.specification.validator;

import java.util.Objects;

import org.radarcns.schema.specification.source.Topic;
import org.radarcns.schema.specification.util.Utils;
import org.radarcns.schema.specification.source.Aggregatable;

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
interface AggregatableRoles<T extends Aggregatable> extends GenericRoles<T> {

    /** Messages. */
    enum AggregatableInfo implements Message {
        AGGREGATOR("Kafka aggregator class is invalid, it should must be a valid AVRO "
            .concat("schema located at ").concat(ValidationSupport.Package.AGGREGATOR.getName()).concat(".")),
        TOPICS("Topic set is not compliant with the provided aggregator. ".concat(
            "In case of null aggregator, the set should contain only the input topic. ").concat(
            "In case of non timed aggregator, the set should contain the input ").concat(
            "topic and the correspondent output topic. In case of timed aggregator, ").concat(
            "the set should contain the input topic and all the ").concat(
            Integer.toString(Topic.TimeLabel.values().length)).concat(" related time frame topics."));

        private final String message;

        AggregatableInfo(String message) {
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
     * @param nullable TODO
     * @return TODO
     */
    static GenericRoles<Aggregatable> validateAggregator(boolean nullable) {
        return aggregatable -> nullable || Objects.nonNull(aggregatable.getAggregator())
                && aggregatable.getAggregator().startsWith(Utils.getProjectGroup().concat(
                    ValidationSupport.Package.AGGREGATOR.getName()))
                && ValidationSupport.isValidClass(aggregatable.getAggregator()) ?
                ValidationResult.valid() : ValidationResult.invalid(AggregatableInfo.AGGREGATOR.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Aggregatable> validateTopics() {
        return aggregatable -> {
            boolean check;
            /*if (Objects.isNull(aggregatable.getAggregator())) {
                check = aggregatable.getTopics().size() == 1;
                check = check && aggregatable.getTopics().contains(aggregatable.getInputTopic());
            } else if (Utils.isTimedAggregator(aggregatable.getAggregator())) {
                check = aggregatable.getTopics().size() == 1 + TimeLabel.values().length;
                check = check && aggregatable.getTopics().contains(aggregatable.getInputTopic());
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.TEN_SECOND.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.THIRTY_SECOND.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.ONE_MIN.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.TEN_MIN.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.ONE_HOUR.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.ONE_DAY.getLabel()));
                check = check && aggregatable.getTopics().contains(
                        aggregatable.getInputTopic().concat(TimeLabel.ONE_WEEK.getLabel()));
            } else {
                check = aggregatable.getTopics().size() == 2;
                check = check && aggregatable.getTopics().contains(aggregatable.getInputTopic());
                check = check && aggregatable.getTopics().contains(
                        Topic.getOutTopic(aggregatable.getInputTopic()));
            }*/
            check = false;

            return check ? ValidationResult.valid() : ValidationResult.invalid(AggregatableInfo.TOPICS.getMessage(
                    /*aggregatable.getTopics().stream().map(Object::toString).collect(
                        Collectors.joining(","))*/
            ));
        };
    }
}
