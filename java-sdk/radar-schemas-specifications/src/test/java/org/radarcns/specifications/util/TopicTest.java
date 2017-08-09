package org.radarcns.specifications.util;

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
import static org.radarcns.specifications.source.Topic.getStateStoreName;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopic;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.radarcns.specifications.source.Topic;

/**
 * TODO.
 */
public class TopicTest {

    @Test
    public void getOutTopicTest() {
        assertEquals("topic_name_output", Topic.getOutTopic("topic_name"));
    }

    @Test
    public void getOutTopicWithTimeTest() {
        Set<String> expected = new HashSet<>();
        expected.add("topic_name_output_10sec");
        expected.add("topic_name_output_30sec");
        expected.add("topic_name_output_1min");
        expected.add("topic_name_output_10min");
        expected.add("topic_name_output_1hour");
        expected.add("topic_name_output_1day");
        expected.add("topic_name_output_1week");

        Set<String> actual = Topic.getTimedOutTopics("topic_name_output");
        assertEquals(expected.size(), actual.size());

        actual.forEach(value -> expected.remove(value));
        assertTrue(expected.isEmpty());
    }

    /*@Test(expected = NullPointerException.class)
    public void getTimeIntervalNullTest() {
        getTimeInterval(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTimeIntervalExceptionTest() {
        getTimeInterval("topic_name_10secs");
    }

    @Test
    public void getTimeIntervalTest() {
        assertEquals(10000, getTimeInterval("topic_name_10sec"), 0);
    }*/

    @Test
    public void getStateStoreNameTest() {
        assertEquals("From-topic_in-To-topic_out",
                getStateStoreName("topic_in", "topic_out"));

        assertTrue(isValidTopic(getStateStoreName("topic_in", "topic_out")));
    }

    @Test(expected = NullPointerException.class)
    public void getStateStoreNullInputTest() {
        getStateStoreName(null, "topic_out");
    }

    @Test(expected = NullPointerException.class)
    public void getStateStoreNullOutputTest() {
        getStateStoreName("topic_in", null);
    }

    @Test(expected = NullPointerException.class)
    public void getStateStoreNullTest() {
        getStateStoreName(null, null);
    }

    /*@Test
    public void getTimedOutputStateStoreTopicTest() {
        Set<String> expected = new HashSet<>();
        expected.add("topic_name_10sec");
        expected.add("From-topic_name-To-topic_name_10sec");
        expected.add("topic_name_10sec");
        expected.add("From-topic_name-To-topic_name_10sec");
        expected.add("topic_name_30sec");
        expected.add("From-topic_name-To-topic_name_30sec");
        expected.add("topic_name_1min");
        expected.add("From-topic_name-To-topic_name_1min");
        expected.add("topic_name_10min");
        expected.add("From-topic_name-To-topic_name_10min");
        expected.add("topic_name_1hour");
        expected.add("From-topic_name-To-topic_name_1hour");
        expected.add("topic_name_1day");
        expected.add("From-topic_name-To-topic_name_1day");
        expected.add("topic_name_1week");
        expected.add("From-topic_name-To-topic_name_1week");

        assertEquals(expected, Topic.getTimedOutputStateStoreTopics("topic_name"));
    }*/

}
