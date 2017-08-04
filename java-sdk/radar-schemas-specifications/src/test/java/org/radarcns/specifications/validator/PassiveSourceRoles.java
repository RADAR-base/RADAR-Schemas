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
import org.radarcns.specifications.source.passive.PassiveSource;
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
        APP_PROVIDER("App provider cannot be null and must be equal to one of the following "
                .concat(allowedProvider().stream().map(
                        Object::toString).collect(Collectors.joining(","))).concat(".")),
        TOPICS("There is no correspondence between topics set at source level al the union of all "
                + "sensors and processors topic sets."),
        TYPE("Passive Source Type cannot be null and should be equal to vendor concatenated to "
                + "name in uppercase."),
        VENDOR_AND_NAME("Vendor and name values cannot be null. Vendor concatenated to name must "
                + "be equal to the source file name.");

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
            valid() : invalid(PassiveSourceInfo.APP_PROVIDER.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateTopics() {
        return passive -> {
                Set<String> topics = passive.getTopics();
                boolean check = Objects.nonNull(topics)
                        && !topics.isEmpty()
                        && passive.getSensors().stream().allMatch(
                            sensor -> topics.containsAll(sensor.getTopics()));

                if (Objects.nonNull(passive.getProcessors())) {
                    check = check &&  passive.getProcessors().stream().allMatch(
                            processor -> topics.containsAll(processor.getTopics()));
                }

                return check ? valid() : invalid(PassiveSourceInfo.TOPICS.getMessage());
        };
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateSourceType() {
        return passive -> Objects.nonNull(passive.getType()) ?
            valid() : invalid(PassiveSourceInfo.TYPE.getMessage());
    }

    /**
     * TODO.
     * @param file
     * @return TODO
     */
    static GenericRoles<PassiveSource> validateModelAndVendor(File file) {
        return passive -> Objects.nonNull(passive.getVendor())
                && Objects.nonNull(passive.getModel())
                && removeExtension(file, YAML_EXTENSION).equalsIgnoreCase(
                        passive.getVendor().toLowerCase().concat(
                                passive.getModel().toLowerCase())) ? valid()
                : invalid(PassiveSourceInfo.VENDOR_AND_NAME.getMessage());
    }
}
