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

package org.radarcns.schema.validation.rules;

import org.radarcns.catalogue.ProcessingState;
import org.radarcns.schema.specification.passive.PassiveDataTopic;

import java.util.Arrays;

import static org.radarcns.schema.util.Utils.testOrFalse;
import static org.radarcns.schema.validation.rules.PassiveSourceRoles.RADAR_PROVIDERS;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;
import static org.radarcns.schema.validation.rules.Validator.validateOrNull;

/**
 * TODO.
 */
public final class SensorRoles {
    private static final String APP_PROVIDER =
            "App provider must be equal to one of the following: " + RADAR_PROVIDERS +  ".";
    private static final String DATA_TYPE =
            "PassiveDataTopic data type cannot be null and should differ from "
            + ProcessingState.UNKNOWN.name() + ".";
    private static final String NAME =
            "PassiveDataTopic name cannot be not null and should be one of "
                    + Arrays.toString(PassiveDataTopic.RadarSourceDataTypes.values()) + ".";

    private SensorRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveDataTopic> validateAppProvider() {
        return validateOrNull(PassiveDataTopic::getAppProvider, RADAR_PROVIDERS::contains,
                APP_PROVIDER);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveDataTopic> validateDataType() {
        return validateNonNull(PassiveDataTopic::getProcessingState,
            state -> !state.name().equals(ProcessingState.UNKNOWN.name()), DATA_TYPE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<PassiveDataTopic> validateName() {
        return validateNonNull(PassiveDataTopic::getType,
                testOrFalse(name -> {
                    // is a valid RADAR source
                    PassiveDataTopic.RadarSourceDataTypes.valueOf(name);
                    return true;
                }), NAME);
    }
}
