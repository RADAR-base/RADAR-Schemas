package org.radarcns.specifications.validator;

import static org.radarcns.specifications.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.ValidationSupport.removeExtension;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.catalogue.DataType;
import org.radarcns.specifications.source.passive.MonitorSource;

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
interface MonitorRoles extends GenericRoles<MonitorSource> {

    String APP_PROVIDER_NAME = "org.radarcns.application.ApplicationServiceProvider";

    /** Messages. */
    enum MonitorInfo implements Message {
        APP_PROVIDER("App provider cannot be null and must be equal to ".concat(
                APP_PROVIDER_NAME).concat(".")),
        DATA_TYPE("The only data type should be ".concat(DataType.RAW.name()).concat(".")),
        NOT_AGGREGATOR("Aggregator is not defined yet."),
        SOURCE_TYPE("Source type cannot be null and should match the file name."),
        TOPICS("Topic set is invalid. It should contain only to the topic specified in "
                + "the configuration file.");

        private final String message;

        MonitorInfo(String message) {
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
    static GenericRoles<MonitorSource> validateAggregator() {
        return monitor -> Objects.isNull(monitor.getAggregator()) ? valid()
                : invalid(MonitorInfo.NOT_AGGREGATOR.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateAppProvider() {
        return monitor -> Objects.nonNull(monitor.getAppProvider())
                && monitor.getAppProvider().equals(APP_PROVIDER_NAME) ?
                valid() : invalid(MonitorInfo.APP_PROVIDER.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateDataType() {
        return monitor -> Objects.nonNull(monitor.getDataType())
                && monitor.getDataType().name().equals(DataType.RAW.name()) ?
                valid() : invalid(MonitorInfo.DATA_TYPE.getMessage());
    }

    /**
     * TODO.
     * @param file
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateSourceType(File file) {
        return monitor -> Objects.nonNull(monitor.getType())
                && removeExtension(file, YAML_EXTENSION).equalsIgnoreCase(monitor.getType().name())?
                valid() : invalid(MonitorInfo.SOURCE_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateTopics() {
        return monitor -> {
            Set<String> input = monitor.getTopics();
            return Objects.nonNull(input) && input.size() == 1
                && input.contains(monitor.getTopic()) ?
                valid() : invalid(MonitorInfo.TOPICS.getMessage(
                        input == null ? "" : input.stream().map(Object::toString).collect(
                            Collectors.joining(","))));
        };
    }
}
