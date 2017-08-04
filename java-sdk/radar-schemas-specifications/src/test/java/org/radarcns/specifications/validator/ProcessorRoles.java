package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.PassiveSourceRoles.allowedProvider;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;
import org.radarcns.specifications.util.TopicUtils;
import org.radarcns.specifications.util.TopicUtils.TimeLabel;
import org.radarcns.specifications.util.Utils;
import org.radarcns.specifications.validator.PassiveSourceRoles.PassiveSourceInfo;

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
interface ProcessorRoles extends GenericRoles<Processor> {

    /** Messages. */
    enum ProcessorInfo implements Message {
        DATA_TYPE("Processor data type should be equal to ".concat(
                DataType.RADAR.name()).concat(".")),
        NAME("Processor name must be not null and different from ".concat(
                SensorName.UNKNOWN.name()).concat(".")),
        OUTPUT("Processor output base topic cannot be null."),
        TOPICS("Topic set is not compliant with the provided aggregator. In case of non "
                + "timed aggregator, it should contain the input topic and the correspondent "
                + "output topic. In case of timed aggregator, then the set should contain the "
                + "input topic name and all the 7 related time frame topics.");

        private final String message;

        ProcessorInfo(String message) {
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
     * @return TODO
     */
    static GenericRoles<Processor> validateDataType() {
        return processor -> Objects.nonNull(processor.getDataType())
            && processor.getDataType().name().equals(DataType.RADAR.name())
            ? valid() : invalid(ProcessorInfo.DATA_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Processor> validateName() {
        return processor -> Objects.nonNull(processor.getName())
                && !processor.getName().name().equals(SensorName.UNKNOWN.name())
                ? valid() : invalid(ProcessorInfo.NAME.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Processor> validateBaseOutputTopic() {
        return processor -> Objects.nonNull(processor.getBaseOutputTopic())
            ? valid() : invalid(ProcessorInfo.OUTPUT.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Processor> validateTopics() {
        return processor -> {
            boolean check = true;
            Set<String> topics = processor.getTopics();
            if (Utils.isTimedAggregator(processor.getAggregator())) {
                check = check && topics.size() == 1 + TimeLabel.values().length;
                check = check && topics.contains(processor.getInputTopic());
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.TEN_SECOND.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.THIRTY_SECOND.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.ONE_MIN.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.TEN_MIN.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.ONE_HOUR.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.ONE_DAY.getLabel()));
                check = check && topics.contains(processor.getBaseOutputTopic().concat(
                        TimeLabel.ONE_WEEK.getLabel()));
            } else {
                check = check && topics.size() == 2;
                check = check && topics.contains(processor.getInputTopic());
                check = check && topics.contains(TopicUtils.getOutTopic(
                        processor.getBaseOutputTopic()));
            }

            return check ? valid() : invalid(ProcessorInfo.TOPICS.getMessage(
                    topics.stream().map(Object::toString).collect(Collectors.joining(","))
            ));
        };
    }
}
