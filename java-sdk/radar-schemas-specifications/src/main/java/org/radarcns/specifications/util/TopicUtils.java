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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.radarcns.catalogue.TimeFrame;

/**
 * TODO.
 */
public final class TopicUtils {

    public static final String FROM_LABEL = "From-";
    public static final String TO_LABEL = "-To-";

    public static final String OUTPUT_LABEL = "_output";

    /** Folder names. */
    public enum TimeLabel {
        TEN_SECOND(TimeFrame.TEN_SECOND, TimeUnit.SECONDS.toMillis(10), "_10sec"),
        THIRTY_SECOND(TimeFrame.THIRTY_SECOND, TimeUnit.SECONDS.toMillis(30), "_30sec"),
        ONE_MIN(TimeFrame.ONE_MIN, TimeUnit.MINUTES.toMillis(1), "_1min"),
        TEN_MIN(TimeFrame.TEN_MIN, TimeUnit.MINUTES.toMillis(10), "_10min"),
        ONE_HOUR(TimeFrame.ONE_HOUR, TimeUnit.HOURS.toMillis(1), "_1hour"),
        ONE_DAY(TimeFrame.ONE_DAY, TimeUnit.DAYS.toMillis(1), "_1day"),
        ONE_WEEK(TimeFrame.ONE_WEEK, TimeUnit.DAYS.toMillis(7), "_1week");

        private final TimeFrame timeFrame;
        private final long intervalInMilliSec;
        private final String label;

        TimeLabel(TimeFrame timeFrame, long intervalInMilliSec, String label) {
            this.timeFrame = timeFrame;
            this.intervalInMilliSec = intervalInMilliSec;
            this.label = label;
        }

        public TimeFrame getTimeFrame() {
            return timeFrame;
        }

        public long getIntervalInMilliSec() {
            return intervalInMilliSec;
        }

        public String getLabel() {
            return label;
        }
    }

    private TopicUtils() {
        //Static class
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static String getOutTopic(String topicName) {
        return topicName.concat(OUTPUT_LABEL);
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static Set<String> getTimedOutTopics(String topicName) {
        Set<String> set = new HashSet<>();

        for (TimeLabel label : TimeLabel.values()) {
            set.add(topicName.concat(label.getLabel()));
        }

        return set;
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static long getTimeInterval(String topicName) {
        Objects.requireNonNull(topicName);

        for (TimeLabel label : TimeLabel.values()) {
            if (topicName.endsWith(label.getLabel())) {
                return label.getIntervalInMilliSec();
            }
        }

        throw new IllegalArgumentException(
                topicName.concat(" does not have any default time interval."));
    }

    /**
     * Kafka Streams allows for stateful stream processing. The internal state is managed in
     *      so-called state stores. A fault-tolerant state store is an internally created and
     *      compacted changelog topic. This function return the changelog topic name.
     *
     * @param inputTopic {@link String} stating the input topic name read by the stateful stream
     * @param outputTopic {@link String} stating the output topic name written by the stateful
     *      stream
     *
     * @return {@code String} representing the changelog topic name
     */
    public static String getStateStoreName(String inputTopic, String outputTopic) {
        Objects.requireNonNull(inputTopic);
        Objects.requireNonNull(outputTopic);

        return FROM_LABEL + inputTopic + TO_LABEL + outputTopic;
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static Set<String> getTimedOutputStateStoreTopics(String topicName) {
        Set<String> set = new HashSet<>();

        Set<String> output = TopicUtils.getTimedOutTopics(topicName);
        set.addAll(output);
        output.forEach(stateStore -> set.add(TopicUtils.getStateStoreName(topicName, stateStore)));

        return set;
    }
}
