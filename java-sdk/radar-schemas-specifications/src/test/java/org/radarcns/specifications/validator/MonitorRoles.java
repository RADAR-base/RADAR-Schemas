package org.radarcns.specifications.validator;

import static org.radarcns.specifications.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.ValidationSupport.removeExtension;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.radarcns.catalogue.DataType;
import org.radarcns.specifications.source.passive.MonitorSource;
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
interface MonitorRoles extends GenericRoles<MonitorSource> {

    /**
     * TODO.
     * @return TODO
     */
    static Set<String> allowedProvider() {
        return Stream.of(
            Utils.getProjectGroup().concat(".application.ApplicationServiceProvider")
        ).collect(Collectors.toSet());
    }

    /** Messages. */
    enum MonitorInfo implements Message {
        APP_PROVIDER("App provider should be equal to one of the following values".concat(
                allowedProvider().stream().collect(Collectors.joining("'"))).concat(".")),
        DATA_TYPE("The only allowed data type is ".concat(DataType.RAW.name()).concat(".")),
        NOT_AGGREGATOR("Aggregators are not defined yet for ".concat(
                MonitorSource.class.getName()).concat(".")),
        SOURCE_TYPE("Source type should match file name.");

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
        return monitor -> /*Objects.isNull(monitor.getAggregator())*/ false ? valid()
                : invalid(MonitorInfo.NOT_AGGREGATOR.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateAppProvider() {
        return monitor -> Objects.nonNull(monitor.getAppProvider())
                && allowedProvider().contains(monitor.getAppProvider()) ?
                valid() : invalid(MonitorInfo.APP_PROVIDER.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateDataType() {
        return monitor -> /*Objects.nonNull(monitor.getProcessingState())
                && monitor.getProcessingState().name().equals(DataType.RAW.name())*/
                invalid(MonitorInfo.DATA_TYPE.getMessage());
    }

    /**
     * TODO.
     * @param file
     * @return TODO
     */
    static GenericRoles<MonitorSource> validateSourceType(File file) {
        return monitor -> Objects.nonNull(monitor.getType())
                && removeExtension(file, YAML_EXTENSION).equalsIgnoreCase(monitor.getType())
                ? valid() : invalid(MonitorInfo.SOURCE_TYPE.getMessage());
    }
}
