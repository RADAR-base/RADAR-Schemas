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
import static org.radarcns.catalogue.SensorName.HEART_RATE;
import static org.radarcns.catalogue.SensorName.HEART_RATE_FILTERED;
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
public class Pebble2Test {

    private static PassiveSource pebble2;
    private static int countSensors;

    @BeforeClass
    public static void initSource() {
        pebble2 = SourceCatalogue.getPassiveSource(PassiveSourceType.PEBBLE_2);
        countSensors = 0;
    }

    @Test
    public void validateHeader() {
        assertEquals("PEBBLE", pebble2.getVendor());
        assertEquals("2", pebble2.getModel());
        assertEquals(PassiveSourceType.PEBBLE_2.name(), pebble2.getType().name());
        assertEquals(PassiveSourceType.PEBBLE_2.name(), pebble2.getName());

        assertEquals("org.radarcns.pebble.PebbleServiceProvider",
            pebble2.getAppProvider());
    }

    @Test
    public void validateAccelerometer() {
        countSensors++;
        assertTrue(testSensorTimed(pebble2.getSensor(ACCELEROMETER),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            null,
            "android_pebble_2_acceleration",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.pebble.Pebble2Acceleration",
            ACCELEROMETER, DataType.RAW, Unit.G, 10d,
            "android_pebble_2_acceleration"
        ));
    }

    @Test
    public void validateBattery() {
        countSensors++;
        assertTrue(testSensorTimed(pebble2.getSensor(BATTERY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_pebble_2_battery_level",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.pebble.Pebble2BatteryLevel",
            BATTERY, DataType.RAW, Unit.PERCENTAGE, 1d,
            "android_pebble_2_battery_level"
        ));
    }

    @Test
    public void validateHeartRate() {
        countSensors++;
        assertTrue(testSensorTimed(pebble2.getSensor(HEART_RATE),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_pebble_2_heartrate",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.pebble.Pebble2HeartRate",
            HEART_RATE, DataType.VENDOR, Unit.BEATS_PER_MIN, 1d,
            "android_pebble_2_heartrate"
        ));
    }

    @Test
    public void validateHeartRateFiltered() {
        countSensors++;
        assertTrue(testSensorTimed(pebble2.getSensor(HEART_RATE_FILTERED),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            null,
            "android_pebble_2_heartrate_filtered",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.pebble.Pebble2HeartRateFiltered",
            HEART_RATE_FILTERED, DataType.VENDOR, Unit.BEATS_PER_MIN, 1d,
            "android_pebble_2_heartrate_filtered"
        ));
    }

    @Test
    public void validateProcessors() {
        assertTrue(pebble2.getProcessors().isEmpty());
    }

    @Test
    public void validateTopics() {
        assertTrue(testSourceTopics(pebble2));
    }

    @AfterClass
    public static void countSensorTest() {
        if (pebble2.getSensors().size() != countSensors) {
            throw new IllegalStateException("The amount of tested sensors do not match the "
                + "specified sensors.");
        }
    }

}
