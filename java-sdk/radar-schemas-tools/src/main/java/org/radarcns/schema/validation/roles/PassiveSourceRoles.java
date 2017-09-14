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

package org.radarcns.schema.validation.roles;

import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.passive.PassiveSource;
import org.radarcns.schema.specification.Utils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.radarcns.schema.validation.ValidationSupport.equalsFileName;
import static org.radarcns.schema.validation.roles.Validator.validate;
import static org.radarcns.schema.validation.roles.Validator.validateNonNull;
import static org.radarcns.schema.validation.roles.Validator.validateOrNull;

/**
 * TODO.
 */
public final class PassiveSourceRoles {
    public static final Set<String> RADAR_PROVIDERS = new HashSet<>(Arrays.asList(
            Utils.getProjectGroup() + ".phone.PhoneLocationProvider",
            Utils.getProjectGroup() + ".phone.PhoneLogProvider",
            Utils.getProjectGroup() + ".phone.PhoneUsageProvider",
            Utils.getProjectGroup() + ".phone.PhoneSensorProvider",
            Utils.getProjectGroup() + ".biovotion.BiovotionServiceProvider",
            Utils.getProjectGroup() + ".empatica.E4ServiceProvider",
            Utils.getProjectGroup() + ".pebble.PebbleServiceProvider"
    ));

    /** Messages. */
    private static final String APP_PROVIDER = "App provider must be equal to one of the following "
            + RADAR_PROVIDERS + ".";
    private static final String SENSORS = "Sensor list cannot be null or empty and cannot contain"
            + " two sensors with the same name";
    private static final String TYPE = "Passive Source Type should be the concatenation of vendor"
            + " and name values in uppercase separated by underscore.";
    private static final String VENDOR_AND_NAME = "Vendor and name values cannot be null."
            + " The concatenation of vendor with \"_\" and name should be equal to the source file"
                + " name in lowercase.";

    private PassiveSourceRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveSource> validateAppProvider() {
        return validateOrNull(PassiveSource::getAppProvider, RADAR_PROVIDERS::contains,
                APP_PROVIDER);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveSource> validateModelAndVendor(Path file) {
        return validate(passive -> Objects.nonNull(passive.getVendor())
                && Objects.nonNull(passive.getModel())
                && equalsFileName(passive.getVendor() + '_' + passive.getModel(),
                        file, SourceCatalogue.YAML_EXTENSION), VENDOR_AND_NAME);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveSource> validateSensors() {
        return validateNonNull(PassiveSource::getSensors, sensors -> !sensors.isEmpty()
                && new HashSet<>(sensors).size() == sensors.size(), SENSORS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveSource> validateSourceType() {
        return validateNonNull(PassiveSource::getType, TYPE);
    }
}
