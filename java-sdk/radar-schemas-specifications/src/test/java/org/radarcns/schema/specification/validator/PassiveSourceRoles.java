package org.radarcns.schema.specification.validator;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.util.Utils;
import org.radarcns.schema.specification.source.passive.PassiveSource;

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
interface PassiveSourceRoles extends GenericRoles<PassiveSource> {

    /**
     * TODO.
     * @return TODO
     */
    static Set<String> allowedProvider() {
        return Stream.of(
            Utils.getProjectGroup().concat(".phone.PhoneLocationProvider"),
            Utils.getProjectGroup().concat(".phone.PhoneLogProvider"),
            Utils.getProjectGroup().concat(".phone.PhoneUsageProvider"),
            Utils.getProjectGroup().concat(".phone.PhoneSensorProvider"),
            Utils.getProjectGroup().concat(".biovotion.BiovotionServiceProvider"),
            Utils.getProjectGroup().concat(".empatica.E4ServiceProvider"),
            Utils.getProjectGroup().concat(".pebble.PebbleServiceProvider")
        ).collect(Collectors.toSet());
    }

    /** Messages. */
    enum PassiveSourceInfo implements Message {
        APP_PROVIDER("App provider must be equal to one of the following ".concat(
                allowedProvider().stream().map(Object::toString).collect(Collectors.joining(
                    ","))).concat(".")),
        SENSORS("Sensor list cannot be null or empty and cannot contain two sensors with".concat(
                "the same name")),
        TOPICS("Union of sensors and processors topic sets does not match topic set".concat(
                "a source level.")),
        TYPE("Passive Source Type should be the concatenation of vendor and name values".concat(
                "in uppercase separated by underscore.")),
        VENDOR_AND_NAME("Vendor and name values cannot be null. The concatenation of ".concat(
                "vendor with \"_\" and name should be equal to the source file").concat(
                " name in lowercase."));

        private final String message;

        PassiveSourceInfo(String message) {
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
    static GenericRoles<PassiveSource> validateAppProvider() {
        return passive -> Objects.isNull(passive.getAppProvider())
                || allowedProvider().contains(passive.getAppProvider()) ?
                ValidationResult.valid() : ValidationResult.invalid(PassiveSourceInfo.APP_PROVIDER.getMessage());
    }

    /**
     * TODO.
     * @param file
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateModelAndVendor(File file) {
        return passive -> Objects.nonNull(passive.getVendor())
                && Objects.nonNull(passive.getModel())
                && ValidationSupport.removeExtension(file, SourceCatalogue.YAML_EXTENSION).equalsIgnoreCase(
                passive.getType().toLowerCase()) ? ValidationResult.valid()
                : ValidationResult.invalid(PassiveSourceInfo.VENDOR_AND_NAME.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateSensors() {
        return passive -> {
            boolean check = Objects.nonNull(passive.getSensors());
            check = check && !passive.getSensors().isEmpty();

            Set<String> temp = new HashSet<>();
            check = check && passive.getSensors().stream().allMatch(
                    sensor -> !temp.add(sensor.getName().name()));

            return check ? ValidationResult.valid() : ValidationResult.invalid(PassiveSourceInfo.SENSORS.getMessage());
        };
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateSourceType() {
        return passive -> Objects.nonNull(passive.getType()) ?
                ValidationResult.valid() : ValidationResult.invalid(PassiveSourceInfo.TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateTopics() {
        return passive -> {
            /*Set<String> topics = passive.getTopics();
            boolean check = Objects.nonNull(topics)
                && !topics.isEmpty()
                && passive.getSensors().stream().allMatch(sensor ->
                topics.containsAll(sensor.getTopics()));

            check = check && Objects.nonNull(passive.getProcessors())
                && passive.getProcessors().stream().allMatch(processor ->
                topics.containsAll(processor.getTopics()));

            return check ? valid() : invalid(PassiveSourceInfo.TOPICS.getMessage());*/
            return false ? ValidationResult.valid() : ValidationResult.invalid(PassiveSourceInfo.TOPICS.getMessage());
        };
    }
}
