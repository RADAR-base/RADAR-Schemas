package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.PassiveSourceRoles.allowedProvider;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;

import java.util.Objects;
import java.util.stream.Collectors;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.specifications.source.passive.Sensor;

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
interface SensorRoles extends TopicRoles {

    /** Messages. */
    enum SensorInfo implements Message {
        APP_PROVIDER("App provider must be equal to one of the following ".concat(
            allowedProvider().stream().map(Object::toString).collect(Collectors.joining(
                ","))).concat(".")),
        DATA_TYPE("Sensor data type cannot be null and should differ from ".concat(
                DataType.UNKNOWN.name()).concat(".")),
        NAME("Sensor name cannot be not null and should different from ".concat(
                SensorName.UNKNOWN.name()).concat("."));

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
            valid() : invalid(SensorInfo.APP_PROVIDER.getMessage());
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
}
