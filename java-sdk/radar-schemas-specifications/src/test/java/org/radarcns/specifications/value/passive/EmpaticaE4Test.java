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
import static org.radarcns.catalogue.SensorName.BLOOD_VOLUME_PULSE;
import static org.radarcns.catalogue.SensorName.ELECTRODERMAL_ACTIVITY;
import static org.radarcns.catalogue.SensorName.HEART_RATE;
import static org.radarcns.catalogue.SensorName.INTER_BEAT_INTERVAL;
import static org.radarcns.catalogue.SensorName.THERMOMETER;
import static org.radarcns.specifications.value.Utility.testSensorTimed;
import static org.radarcns.specifications.value.Utility.testSourceTopics;
import static org.radarcns.specifications.value.Utility.testTopicTimedAggregator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.SourceCatalogue;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;

/**
 * TODO.
 */
public class EmpaticaE4Test {

    private static PassiveSource empaticaE4;
    private static int countSensors;

    @BeforeClass
    public static void initSource() {
        empaticaE4 = SourceCatalogue.getPassiveSource(PassiveSourceType.EMPATICA_E4);
        countSensors = 0;
    }

    @Test
    public void validateHeader() {
        assertEquals("EMPATICA", empaticaE4.getVendor());
        assertEquals("E4", empaticaE4.getModel());
        assertEquals(PassiveSourceType.EMPATICA_E4.name(), empaticaE4.getType().name());
        assertEquals(PassiveSourceType.EMPATICA_E4.name(), empaticaE4.getName());

        assertEquals("org.radarcns.empatica.E4ServiceProvider",
            empaticaE4.getAppProvider());
    }

    @Test
    public void validateAccelerometer() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(ACCELEROMETER),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_empatica_e4_acceleration",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4Acceleration",
            ACCELEROMETER, DataType.RAW, Unit.G, 32d,
            "android_empatica_e4_acceleration"
        ));
    }

    @Test
    public void validateBattery() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(BATTERY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_empatica_e4_battery_level",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4BatteryLevel",
            BATTERY, DataType.RAW, Unit.PERCENTAGE, 1d,
            "android_empatica_e4_battery_level"
        ));
    }

    @Test
    public void validateBloodVolumePulse() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(BLOOD_VOLUME_PULSE),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_empatica_e4_blood_volume_pulse",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4BloodVolumePulse",
            BLOOD_VOLUME_PULSE, DataType.RAW, Unit.NANO_WATT, 64d,
            "android_empatica_e4_blood_volume_pulse"
        ));
    }

    @Test
    public void validateElectroDermalActivity() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(ELECTRODERMAL_ACTIVITY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_empatica_e4_electrodermal_activity",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4ElectroDermalActivity",
            ELECTRODERMAL_ACTIVITY, DataType.RAW, Unit.MICRO_SIEMENS, 4d,
            "android_empatica_e4_electrodermal_activity"
        ));
    }

    @Test
    public void validateInterBeatInterval() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(INTER_BEAT_INTERVAL),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_empatica_e4_inter_beat_interval",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4InterBeatInterval",
            INTER_BEAT_INTERVAL, DataType.VENDOR, Unit.BEATS_PER_MIN, 1d,
            "android_empatica_e4_inter_beat_interval"
        ));
    }

    @Test
    public void validateTemperature() {
        countSensors++;
        assertTrue(testSensorTimed(empaticaE4.getSensor(THERMOMETER),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_empatica_e4_temperature",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4Temperature",
            THERMOMETER, DataType.RAW, Unit.CELSIUS, 4d,
            "android_empatica_e4_temperature"
        ));
    }

    @Test
    public void validateProcessors() {
        assertEquals(1, empaticaE4.getProcessors().size(), 0);
        Processor processor = empaticaE4.getProcessor(HEART_RATE);
        assertEquals("org.radarcns.kafka.aggregator.AggregatorDouble",
                processor.getTopic().getAggregator());
        assertEquals(DataType.RADAR.name(), processor.getProcessingState().name());
        //processor.getDoc();
        assertEquals(HEART_RATE.name(), processor.getName());
        assertEquals(1.0, processor.getSampleRate(), 0.0);
        assertEquals(Unit.BEATS_PER_MIN.name(), processor.getUnit());
        testTopicTimedAggregator(
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            "android_empatica_e4_inter_beat_interval",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.empatica.EmpaticaE4InterBeatInterval",
            processor.getTopic(),
            "android_empatica_e4_heartrate");
    }

    @Test
    public void validateTopics() {
        assertTrue(testSourceTopics(empaticaE4));
    }

    @AfterClass
    public static void countSensorTest() {
        if (empaticaE4.getSensors().size() != countSensors) {
            throw new IllegalStateException("The amount of tested sensors do not match the "
                + "specified sensors.");
        }
    }

}
