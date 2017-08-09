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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.catalogue.SensorName.ACCELEROMETER;
import static org.radarcns.catalogue.SensorName.BATTERY;
import static org.radarcns.catalogue.SensorName.GYROSCOPE;
import static org.radarcns.catalogue.SensorName.LIGHT;
import static org.radarcns.catalogue.SensorName.MAGNETIC_FIELD;
import static org.radarcns.catalogue.SensorName.PHONE_CALL;
import static org.radarcns.catalogue.SensorName.PHONE_SMS;
import static org.radarcns.catalogue.SensorName.PHONE_SMS_UNREAD;
import static org.radarcns.catalogue.SensorName.RELATIVE_LOCATION;
import static org.radarcns.catalogue.SensorName.STEP_COUNT;
import static org.radarcns.catalogue.SensorName.USAGE_EVENT;
import static org.radarcns.catalogue.SensorName.USER_INTERACTION;
import static org.radarcns.specifications.value.Utility.testSensorNonAggregatable;
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
public class AndroidPhoneTest {

    private static PassiveSource androidPhone;
    private static int countSensors;

    @BeforeClass
    public static void initSource() {
        androidPhone = SourceCatalogue.getPassiveSource(PassiveSourceType.ANDROID_PHONE);
        countSensors = 0;
    }

    @Test
    public void validateHeader() {
        assertEquals("ANDROID", androidPhone.getVendor());
        assertEquals("PHONE", androidPhone.getModel());
        assertEquals(PassiveSourceType.ANDROID_PHONE.name(), androidPhone.getType().name());
        assertEquals(PassiveSourceType.ANDROID_PHONE.name(), androidPhone.getName());

        assertNull(androidPhone.getAppProvider());
    }

    @Test
    public void validatePhoneSensors() {
        String expectedAppProvider = "org.radarcns.phone.PhoneSensorProvider";

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(ACCELEROMETER),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            expectedAppProvider,
            "android_phone_acceleration",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneAcceleration",
            ACCELEROMETER, DataType.RAW, Unit.G, -1d,
            "android_phone_acceleration"
        ));

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(BATTERY),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            expectedAppProvider,
            "android_phone_battery_level",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneBatteryLevel",
            BATTERY, DataType.RAW, Unit.PERCENTAGE, -1d,
            "android_phone_battery_level"
        ));

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(GYROSCOPE),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            expectedAppProvider,
            "android_phone_gyroscope",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneGyroscope",
            GYROSCOPE, DataType.RAW, Unit.RADAIAN_PER_SEC, -1d,
            "android_phone_gyroscope"
        ));

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(LIGHT),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            expectedAppProvider,
            "android_phone_light",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneLight",
            LIGHT, DataType.RAW, Unit.LUX, -1d,
            "android_phone_light"
        ));

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(MAGNETIC_FIELD),
            "org.radarcns.kafka.aggregator.AggregatorDoubleArray",
            expectedAppProvider,
            "android_phone_magnetic_field",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneMagneticField",
            MAGNETIC_FIELD, DataType.RAW, Unit.MICRO_TESLA, -1d,
            "android_phone_magnetic_field"
        ));

        countSensors++;
        assertTrue(testSensorTimed(androidPhone.getSensor(STEP_COUNT),
            "org.radarcns.kafka.aggregator.AggregatorDouble",
            expectedAppProvider,
            "android_phone_step_count",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneStepCount",
            STEP_COUNT, DataType.VENDOR, Unit.NON_DIMENSIONAL, -1d,
            "android_phone_step_count"
        ));
    }

    @Test
    public void validatePhoneLocation() {
        String expectedAppProvider = "org.radarcns.phone.PhoneLocationProvider";

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(RELATIVE_LOCATION),
            expectedAppProvider,
            "android_phone_relative_location",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneRelativeLocation",
            RELATIVE_LOCATION, DataType.RAW, Unit.DEGREE, -1d
        ));
    }

    @Test
    public void validatePhoneLogs() {
        String expectedAppProvider = "org.radarcns.phone.PhoneLogProvider";

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(PHONE_CALL),
            expectedAppProvider,
            "android_phone_call",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneCall",
            PHONE_CALL, DataType.RAW, Unit.NON_DIMENSIONAL, -1d
        ));

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(PHONE_SMS),
            expectedAppProvider,
            "android_phone_sms",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneSms",
            PHONE_SMS, DataType.RAW, Unit.NON_DIMENSIONAL, -1d
        ));

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(PHONE_SMS_UNREAD),
            expectedAppProvider,
            "android_phone_sms_unread",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneSmsUnread",
            PHONE_SMS_UNREAD, DataType.RAW, Unit.NON_DIMENSIONAL, -1d
        ));
    }

    @Test
    public void validatePhoneUsage() {
        String expectedAppProvider = "org.radarcns.phone.PhoneUsageProvider";

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(USAGE_EVENT),
            expectedAppProvider,
            "android_phone_usage_event",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneUsageEvent",
            USAGE_EVENT, DataType.RAW, Unit.NON_DIMENSIONAL, -1d
        ));

        countSensors++;
        assertTrue(testSensorNonAggregatable(androidPhone.getSensor(USER_INTERACTION),
            expectedAppProvider,
            "android_phone_user_interaction",
            "org.radarcns.kafka.key.KeyMeasurement",
            "org.radarcns.passive.phone.PhoneUserInteraction",
            USER_INTERACTION, DataType.RAW, Unit.NON_DIMENSIONAL, -1d
        ));
    }

    @Test
    public void validateTopics() {
        assertTrue(testSourceTopics(androidPhone));
    }

    /*@AfterClass
    public static void countSensorTest() {
        if (androidPhone.getSensors().size() != countSensors) {
            throw new IllegalStateException("The amount of tested sensors do not match the "
                + "specified sensors.");
        }
    }*/

}
