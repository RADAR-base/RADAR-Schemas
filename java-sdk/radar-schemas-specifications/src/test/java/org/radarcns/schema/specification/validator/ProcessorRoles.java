package org.radarcns.schema.specification.validator;

import org.radarcns.catalogue.ProcessingState;
import org.radarcns.catalogue.SensorName;
import org.radarcns.schema.specification.source.passive.Processor;

import java.util.Objects;

import static org.radarcns.schema.specification.validator.ValidationResult.invalid;
import static org.radarcns.schema.specification.validator.ValidationResult.valid;

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
                ProcessingState.RADAR.name()).concat(".")),
        NAME("Processor name should be not null and different from ".concat(
                SensorName.UNKNOWN.name()).concat(".")),
        OUTPUT("Processor base output topic is invalid.");

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
        return processor -> Objects.nonNull(processor.getProcessingState())
                && processor.getProcessingState().name().equals(ProcessingState.RADAR.name())
                ? valid() : invalid(ProcessorInfo.DATA_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Processor> validateName() {
        return processor -> Objects.nonNull(processor.getName())
                && !processor.getName().equals(SensorName.UNKNOWN.name())
                ? valid() : invalid(ProcessorInfo.NAME.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Processor> validateBaseOutputTopic() {
        return processor -> /*Objects.nonNull(processor.getBaseOutputTopic())
                && isValidTopic(processor.getBaseOutputTopic())*/
                false ? valid() : invalid(ProcessorInfo.OUTPUT.getMessage(
                        /*isValidTopicVerbose(processor.getBaseOutputTopic())*/));
    }
}
