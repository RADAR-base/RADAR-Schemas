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

package org.radarcns.schema.specification.passive;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.catalogue.ProcessingState;
import org.radarcns.schema.specification.AppDataTopic;

import java.util.Objects;

/**
 * TODO.
 */
public class PassiveDataTopic extends AppDataTopic {
    public enum RadarSourceDataTypes {
        ACCELEROMETER, BATTERY, BLOOD_VOLUME_PULSE, BLOOD_PULSE_WAVE, ELECTRODERMAL_ACTIVITY,
        ENERGY, GALVANIC_SKIN_RESPONSE, GYROSCOPE, HEART_RATE, HEART_RATE_FILTERED,
        HEART_RATE_VARIABILITY, INTER_BEAT_INTERVAL, LED, LIGHT, MAGNETIC_FIELD, OXYGEN_SATURATION,
        PHONE_CALL, PHONE_SMS, PHONE_BLUETOOTH_DEVICES, PHONE_SMS_UNREAD, PHONE_CONTACTS,
        PHOTOPLETHYSMOGRAPHY, RELATIVE_LOCATION, RESPIRATION_RATE, STEP_COUNT, THERMOMETER,
        USAGE_EVENT, USER_INTERACTION
    }

    @JsonProperty("processing_state")
    private ProcessingState processingState;

    public ProcessingState getProcessingState() {
        return processingState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        PassiveDataTopic passiveData = (PassiveDataTopic) o;
        return Objects.equals(processingState, passiveData.processingState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processingState);
    }
}
