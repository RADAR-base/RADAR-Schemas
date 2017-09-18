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
import org.radarcns.catalogue.SensorName;
import org.radarcns.schema.specification.passive.Processor;

import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public final class ProcessorRoles {

    private static final String DATA_TYPE = "Processor data type should be equal to "
                + ProcessingState.RADAR.name() + ".";
    private static final String NAME = "Processor name should be not null and different from "
                + SensorName.UNKNOWN.name() + ".";

    private ProcessorRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Processor> validateDataType() {
        return validateNonNull(Processor::getProcessingState, ProcessingState.RADAR::equals,
                DATA_TYPE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Processor> validateName() {
        return validateNonNull(Processor::getName,
                name -> !name.equalsIgnoreCase(SensorName.UNKNOWN.name()), NAME);
    }
}
