package org.radarcns.specifications.value.passive;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.radarcns.catalogue.SensorName.ACCELEROMETER;
import static org.radarcns.catalogue.SensorName.BATTERY;
import static org.radarcns.catalogue.SensorName.BLOOD_PULSE_WAVE;
import static org.radarcns.catalogue.SensorName.ENERGY;
import static org.radarcns.catalogue.SensorName.GALVANIC_SKIN_RESPONSE;
import static org.radarcns.catalogue.SensorName.HEART_RATE;
import static org.radarcns.catalogue.SensorName.HEART_RATE_VARIABILITY;
import static org.radarcns.catalogue.SensorName.LED;
import static org.radarcns.catalogue.SensorName.OXYGEN_SATURATION;
import static org.radarcns.catalogue.SensorName.PHOTOPLETHYSMOGRAPHY;
import static org.radarcns.catalogue.SensorName.RESPIRATION_RATE;
import static org.radarcns.catalogue.SensorName.THERMOMETER;
import static org.radarcns.specifications.value.Utility.testSensorTimed;
import static org.radarcns.specifications.value.Utility.testSourceTopics;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.SourceCatalogue;
import org.radarcns.specifications.source.passive.PassiveSource;

/**
 * TODO.
 */
public class BiovotionTest {

    private static PassiveSource biovotion;
    private static int countSensors;

    @BeforeClass
    public static void initSource() {
        biovotion = SourceCatalogue.getPassiveSource(PassiveSourceType.BIOVOTION_VSM1);
        countSensors = 0;
    }

    @Test
    public void validateHeader() {
        assertEquals("BIOVOTION", biovotion.getVendor());
        assertEquals("VSM1", biovotion.getModel());
        assertEquals(PassiveSourceType.BIOVOTION_VSM1.name(), biovotion.getType().name());
        assertEquals(PassiveSourceType.BIOVOTION_VSM1.name(), biovotion.getName());

        assertEquals("org.radarcns.biovotion.BiovotionServiceProvider",
            biovotion.getAppProvider());
    }

    @Test
    public void validateAcceleration() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(ACCELEROMETER),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_biovotion_vsm1_acceleration",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1Acceleration",
            ACCELEROMETER, DataType.RAW, Unit.G, 51.2,
            "android_biovotion_vsm1_acceleration"
        ));
    }

    @Test
    public void validateBatteryLevel() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(BATTERY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_battery_level",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1BatteryLevel",
            BATTERY, DataType.RAW, Unit.PERCENTAGE, 1d,
            "android_biovotion_vsm1_battery_level"
        ));
    }

    @Test
    public void validateBloodPulseWave() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(BLOOD_PULSE_WAVE),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_blood_volume_pulse",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1BloodPulseWave",
            BLOOD_PULSE_WAVE, DataType.VENDOR, Unit.NON_DIMENSIONAL, 1d,
            "android_biovotion_vsm1_blood_volume_pulse"
        ));
    }

    @Test
    public void validateEnergy() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(ENERGY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_energy",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1Energy",
            ENERGY, DataType.VENDOR, Unit.CALORIES_PER_SEC, 1d,
            "android_biovotion_vsm1_energy"
        ));
    }

    @Test
    public void validateGalvanicSkinResponse() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(GALVANIC_SKIN_RESPONSE),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_biovotion_vsm1_galvanic_skin_response",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1GalvanicSkinResponse",
            GALVANIC_SKIN_RESPONSE, DataType.VENDOR, Unit.KILO_OHM, 1d,
            "android_biovotion_vsm1_galvanic_skin_response"
        ));
    }

    @Test
    public void validateHeartRate() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(HEART_RATE),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_heartrate",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1HeartRate",
            HEART_RATE, DataType.VENDOR, Unit.BEATS_PER_MIN, 1d,
            "android_biovotion_vsm1_heartrate"
        ));
    }

    @Test
    public void validateHeartRateVariability() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(HEART_RATE_VARIABILITY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_heartrate_variability",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1HeartRateVariability",
            HEART_RATE_VARIABILITY, DataType.VENDOR, Unit.RMSSD_IN_MILLI_SEC, 1d,
            "android_biovotion_vsm1_heartrate_variability"
        ));
    }

    @Test
    public void validateLedCurrent() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(LED),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_biovotion_vsm1_led_current",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1LedCurrent",
            LED, DataType.VENDOR, Unit.MILLI_AMPERE, 1d,
            "android_biovotion_vsm1_led_current"
        ));
    }

    @Test
    public void validatePpgRaw() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(PHOTOPLETHYSMOGRAPHY),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_biovotion_vsm1_ppg_raw",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1PpgRaw",
            PHOTOPLETHYSMOGRAPHY, DataType.RAW, Unit.NON_DIMENSIONAL, 51.2,
            "android_biovotion_vsm1_ppg_raw"
        ));
    }

    @Test
    public void validateRespirationRate() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(RESPIRATION_RATE),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_respiration_rate",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1RespirationRate",
            RESPIRATION_RATE, DataType.VENDOR, Unit.BEATS_PER_MIN, 1d,
            "android_biovotion_vsm1_respiration_rate"
        ));
    }

    @Test
    public void validateOxygenSaturation() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(OXYGEN_SATURATION),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_oxygen_saturation",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1OxygenSaturation",
            OXYGEN_SATURATION, DataType.VENDOR, Unit.PERCENTAGE, 1d,
            "android_biovotion_vsm1_oxygen_saturation"
        ));
    }

    @Test
    public void validateTemperature() {
        countSensors++;
        assertTrue(testSensorTimed(biovotion.getSensor(THERMOMETER),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_biovotion_vsm1_temperature",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.biovotion.BiovotionVsm1Temperature",
            THERMOMETER, DataType.RAW, Unit.CELSIUS, 1d,
            "android_biovotion_vsm1_temperature"
        ));
    }

    @Test
    public void validateProcessors() {
        assertTrue(biovotion.getProcessors().isEmpty());
    }

    @Test
    public void validateTopics() {
        assertTrue(testSourceTopics(biovotion));
    }

    @AfterClass
    public static void countSensorTest() {
        if (biovotion.getSensors().size() != countSensors) {
            throw new IllegalStateException("The amount of tested sensors do not match the "
                + "specified sensors.");
        }
    }

}
