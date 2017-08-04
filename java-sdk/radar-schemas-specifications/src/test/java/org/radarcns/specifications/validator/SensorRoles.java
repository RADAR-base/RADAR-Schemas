package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.PassiveSourceRoles.validateSourceType;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.PassiveSourceRoles.allowedProvider;

import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
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
interface SensorRoles extends GenericRoles<Sensor> {

    /** Messages. */
    enum SensorInfo implements Message {
        DATA_TYPE("Sensor data type cannot be null and should differ from ".concat(
                DataType.UNKNOWN.name()).concat(".")),
        NAME("Sensor name must be not null and different from ".concat(
            SensorName.UNKNOWN.name()).concat(".")),
        TOPICS("Topic set is not compliant with the provided aggregator. In case of null "
                + "aggregator, the set should contain only the provided topic. In case of non "
                + "timed aggregator, it should contain the specified topic and the correspondent "
                + "output topic. In case of timed aggregator, then the set should contain the set "
                + "topic name and all the 7 related time frame topics.");

        private final String message;

        SensorInfo(String message) {
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
    static GenericRoles<Sensor> validateAppProvider() {
        return passive -> Objects.isNull(passive.getAppProvider())
            || allowedProvider().contains(passive.getAppProvider()) ?
            valid() : invalid(PassiveSourceInfo.APP_PROVIDER.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Sensor> validateDataType() {
        return sensor -> Objects.nonNull(sensor.getDataType())
            && !sensor.getDataType().name().equals(DataType.UNKNOWN.name())
            ? valid() : invalid(SensorInfo.DATA_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Sensor> validateName() {
        return sensor -> Objects.nonNull(sensor.getName())
                && !sensor.getName().name().equals(SensorName.UNKNOWN.name())
                ? valid() : invalid(SensorInfo.NAME.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Sensor> validateTopics() {
        return sensor -> {
            boolean check = true;
            Set<String> topics = sensor.getTopics();
            if (Objects.isNull(sensor.getAggregator())) {
                check = check && topics.size() == 1;
                check = check && topics.contains(sensor.getTopic());
            } else if (Utils.isTimedAggregator(sensor.getAggregator())) {
                check = check && topics.size() == 1 + TimeLabel.values().length;
                check = check && topics.contains(sensor.getTopic());
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.TEN_SECOND.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.THIRTY_SECOND.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.ONE_MIN.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.TEN_MIN.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.ONE_HOUR.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.ONE_DAY.getLabel()));
                check = check && topics.contains(sensor.getTopic().concat(
                        TimeLabel.ONE_WEEK.getLabel()));
            } else {
                check = check && topics.size() == 2;
                check = check && topics.contains(sensor.getTopic());
                check = check && topics.contains(TopicUtils.getOutTopic(sensor.getTopic()));
            }

            return check ? valid() : invalid(SensorInfo.TOPICS.getMessage(
                    topics.stream().map(Object::toString).collect(Collectors.joining(","))
            ));
        };
    }
}
