package org.radarcns.specifications.value;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.catalogue.TimeFrame;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.Topic;
import org.radarcns.specifications.source.Topic.TimeLabel;
import org.radarcns.specifications.source.Topic.TopicMetadata;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;

/**
 * TODO.
 */
public class Utility {

    /*topic.getTopicNames();;
    topic.getOutputTopics();*/

    private static boolean testGenericTopic(String inputTopic, String key, String value,
            String aggregator, Topic topic) {
        if (Objects.isNull(aggregator)) {
            assertFalse(topic.hasAggregator());
            assertNull(topic.getAggregator());
        } else {
            assertTrue(topic.hasAggregator());
            assertEquals(aggregator, topic.getAggregator());
        }

        assertEquals(key, topic.getInputKey());
        assertEquals(inputTopic, topic.getInputTopic());
        assertEquals(value, topic.getInputValue());

        return true;
    }

    /**
     * TODO.
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param topic TODO
     */
    public static void testTopicNonAggregatable(String inputTopic, String key, String value,
            Topic topic) {
        assertTrue(testGenericTopic(inputTopic, key, value, null, topic));

        assertTrue(topic.getOutputTopics().isEmpty());
        assertEquals(1, topic.getTopicNames().size(), 0);

        assertTrue("Topics is invalid. ".concat(
                topic.getTopicNames().stream().collect(Collectors.joining(","))),
                topic.getTopicNames().contains(inputTopic));
    }

    /**
     * TODO.
     * @param aggregator TODO
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param topic TODO
     * @param baseOutput TODO
     */
    public static void testTopicNonTimedAggregator(String aggregator, String inputTopic,
            String key, String value, Topic topic, String baseOutput) {
        assertTrue(testGenericTopic(inputTopic, key, value, aggregator, topic));

        assertFalse(topic.getTopicNames().isEmpty());
        assertEquals(2, topic.getTopicNames().size(), 0);

        assertTrue(topic.getTopicNames().contains(inputTopic));
        assertTrue(topic.getTopicNames().contains(baseOutput.concat("_output")));

        assertFalse(topic.getOutputTopics().isEmpty());
        assertEquals(1, topic.getOutputTopics().size(), 0);

        TopicMetadata metadata = (TopicMetadata) topic.getOutputTopics().toArray()[0];
        assertEquals(-1, metadata.getIntervalInMilliSec(), 0);
        assertEquals(inputTopic, metadata.getInput());
        assertEquals(baseOutput.concat("_output"), metadata.getOutput());
        assertEquals("From-" + inputTopic + "-To-" + baseOutput, metadata.getStateStore());
        assertNull(metadata.getTimeFrame().name());
    }

    /**
     * TODO.
     * @param aggregator TODO
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param topic TODO
     * @param baseOutput TODO
     */
    public static void testTopicTimedAggregator(String aggregator, String inputTopic, String key,
            String value, Topic topic, String baseOutput) {
        assertTrue(testGenericTopic(inputTopic, key, value, aggregator, topic));

        Set<String> topicNames = topic.getTopicNames();
        assertFalse(topicNames.isEmpty());
        assertEquals(15, topicNames.size(), 0);
        assertTrue(topicNames.contains(inputTopic));

        assertFalse(topic.getOutputTopics().isEmpty());
        assertEquals(TimeFrame.values().length - 1,
                topic.getOutputTopics().size(), 0);


        for (TimeLabel label : TimeLabel.values()) {
            if (label.getIntervalInMilliSec() != -1) {
                testTopicMetadata(topic, inputTopic, baseOutput, label);
            }
        }
    }

    private static void testTopicMetadata(Topic topic, String inputTopic, String baseOutput,
            TimeLabel label) {
        String output = baseOutput.concat(label.getLabel());
        String stateStore = "From-".concat(inputTopic).concat("-To-").concat(output);



        assertTrue(topic.getTopicNames().contains(stateStore));
        assertTrue(topic.getTopicNames().contains(output));
        boolean canary = false;
        for (TopicMetadata metadata : topic.getOutputTopics()) {
            if (metadata.getTimeFrame().name().equals(label.getTimeFrame().name())) {
                assertEquals(label.getIntervalInMilliSec(), metadata.getIntervalInMilliSec(),
                        0);
                assertEquals(inputTopic, metadata.getInput());
                assertEquals(output, metadata.getOutput());
                assertEquals(stateStore, metadata.getStateStore());
                canary = true;
            }
        }
        assertTrue(canary);
    }

    /**
     * TODO.
     * @param source TODO
     * @return TODO
     */
    public static boolean testSourceTopics(PassiveSource source) {
        Set<String> expectedTopics = new HashSet<>();
        for (Sensor sensor : source.getSensors()) {
            expectedTopics.addAll(sensor.getTopic().getTopicNames());
        }
        for (Processor processor : source.getProcessors()) {
            expectedTopics.addAll(processor.getTopic().getTopicNames());
        }

        expectedTopics.removeAll(source.getTopics());

        return expectedTopics.isEmpty();
    }

    private static boolean testBasicSensor(Sensor sensor, String appProvider,
            String inputTopic, SensorName sensorName, DataType dataType, Unit unit,
            double sampleRate) {
        assertEquals(appProvider, sensor.getAppProvider());
        assertEquals(dataType.name(), sensor.getProcessingState().name());
        //sensor.getDoc()
        assertEquals(sensorName.name(), sensor.getName().name());
        assertEquals(sampleRate, sensor.getSampleRate(), 0);
        assertEquals(unit.name(), sensor.getUnit());

        return true;
    }

    /**
     * TODO.
     * @param sensor TODO
     * @param appProvider TODO
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param sensorName TODO
     * @param dataType TODO
     * @param unit TODO
     * @param sampleRate TODO
     * @return TODO
     */
    public static boolean testSensorNonAggregatable(Sensor sensor, String appProvider,
            String inputTopic, String key, String value, SensorName sensorName, DataType dataType,
            Unit unit, double sampleRate) {
        assertNull(sensor.getTopic().getAggregator());
        testTopicNonAggregatable(inputTopic, key, value, sensor.getTopic());

        return testBasicSensor(sensor, appProvider, inputTopic, sensorName, dataType, unit,
                sampleRate);
    }

    /**
     * TODO.
     * @param sensor TODO
     * @param aggregator TODO
     * @param appProvider TODO
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param sensorName TODO
     * @param dataType TODO
     * @param unit TODO
     * @param sampleRate TODO
     * @param baseOutput TODO
     * @return TODO
     */
    public static boolean testSensorNonTimed(Sensor sensor, String aggregator, String appProvider,
            String inputTopic, String key, String value, SensorName sensorName, DataType dataType,
            Unit unit, double sampleRate, String baseOutput) {
        assertEquals(aggregator, sensor.getTopic().getAggregator());
        testTopicNonTimedAggregator(aggregator, inputTopic, key, value, sensor.getTopic(),
                baseOutput);

        return testBasicSensor(sensor, appProvider, inputTopic, sensorName, dataType, unit,
                sampleRate);
    }

    /**
     * TODO.
     * @param sensor TODO
     * @param aggregator TODO
     * @param appProvider TODO
     * @param inputTopic TODO
     * @param key TODO
     * @param value TODO
     * @param sensorName TODO
     * @param dataType TODO
     * @param unit TODO
     * @param sampleRate TODO
     * @param baseOutput TODO
     * @return TODO
     */
    public static boolean testSensorTimed(Sensor sensor, String aggregator, String appProvider,
            String inputTopic, String key, String value, SensorName sensorName, DataType dataType,
            Unit unit, double sampleRate, String baseOutput) {
        assertEquals(aggregator, sensor.getTopic().getAggregator());
        testTopicTimedAggregator(aggregator, inputTopic, key, value, sensor.getTopic(),
                baseOutput);

        return testBasicSensor(sensor, appProvider, inputTopic, sensorName, dataType, unit,
                sampleRate);
    }


}
