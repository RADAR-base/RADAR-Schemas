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

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.radarcns.kafka.aggregator.AggregatorDouble;
import org.radarcns.kafka.aggregator.AggregatorDoubleArray;

/**
 * TODO.
 */
public class UtilsTest {

    @Test
    public void projectGroupTest() {
        assertEquals("org.radarcns", Utils.getProjectGroup());
    }

    @Test
    public void timedAggregatorTest() {
        assertTrue(Utils.isTimedAggregator(AggregatorDouble.class.getCanonicalName()));
        assertTrue(Utils.isTimedAggregator(AggregatorDoubleArray.class.getCanonicalName()));
        assertFalse(Utils.isTimedAggregator(getClass().getCanonicalName()));
    }

}
