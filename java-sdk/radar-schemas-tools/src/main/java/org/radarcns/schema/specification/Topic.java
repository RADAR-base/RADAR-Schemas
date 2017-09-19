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

package org.radarcns.schema.specification;

import org.radarcns.catalogue.TimeFrame;
import org.radarcns.schema.util.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.radarcns.schema.specification.Source.expandClass;

/**
 * TODO.
 */
public class Topic implements Aggregatable {

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
        ONE_WEEK(TimeFrame.ONE_WEEK, TimeUnit.DAYS.toMillis(7), "_1week"),
        NOT_APPLICABLE(null, -1, null);

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

    private final String inputTopic;
    private final String inputKey;
    private final String inputValue;

    private final String aggregator;

    private final String baseOutput;

    private final Set<TopicMetadata> output;

    /**
     * TODO.
     */
    public class TopicMetadata {
        private final TimeFrame timeFrame;
        private final long intervalInMilliSec;
        private final String input;
        private final String stateStore;
        private final String output;

        public TopicMetadata(TimeLabel timeLabel, String input, String stateStore, String output) {
            this.timeFrame = timeLabel.getTimeFrame();
            this.intervalInMilliSec = timeLabel.getIntervalInMilliSec();
            this.input = input;
            this.stateStore = stateStore;
            this.output = output;
        }

        public TimeFrame getTimeFrame() {
            return timeFrame;
        }

        public long getIntervalInMilliSec() {
            return intervalInMilliSec;
        }

        public String getInput() {
            return input;
        }

        public String getStateStore() {
            return stateStore;
        }

        public String getOutput() {
            return output;
        }
    }

    /**
     * TODO.
     * @param inputTopic TODO
     * @param inputKey TODO
     * @param inputValue TODO
     * @param aggregator TODO
     * @param baseOutput TODO
     */
    public Topic(String inputTopic, String inputKey, String inputValue, String aggregator,
            String baseOutput) {
        Objects.requireNonNull(inputTopic);
        Objects.requireNonNull(inputKey);
        Objects.requireNonNull(inputValue);

        this.inputTopic = inputTopic;
        this.inputKey = expandClass(inputKey);
        this.inputValue = expandClass(inputValue);
        this.aggregator = expandClass(aggregator);

        this.baseOutput = baseOutput != null ? baseOutput : inputTopic;

        this.output = new HashSet<>();

        if (Utils.isTimedAggregator(this.aggregator)) {
            for (TimeLabel label : TimeLabel.values()) {
                if (label.intervalInMilliSec != -1) {
                    this.output.add(new TopicMetadata(label, inputTopic,
                            getStateStoreName(inputTopic, getOutTopic(this.baseOutput, label)),
                            getOutTopic(this.baseOutput, label)));
                }
            }
        } else if (this.aggregator != null) {
            this.output.add(new TopicMetadata(TimeLabel.NOT_APPLICABLE, inputTopic,
                    getStateStoreName(inputTopic, getOutTopic(this.baseOutput)),
                    getOutTopic(this.baseOutput)));
        }
    }

    @Override
    public String getInputTopic() {
        return inputTopic;
    }

    @Override
    public Set<TopicMetadata> getOutputTopics() {
        return output;
    }

    @Override
    public boolean hasAggregator() {
        return aggregator != null;
    }

    @Override
    public String getAggregator() {
        return aggregator;
    }

    /**
     * TODO.
     * @return TODO
     */
    public String getInputKey() {
        return inputKey;
    }

    /**
     * TODO.
     * @return TODO
     */
    public String getInputValue() {
        return inputValue;
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getTopicNames() {
        Set<String> names = new HashSet<>();
        names.add(inputTopic);

        if (hasAggregator()) {
            for (TopicMetadata metadata : output) {
                names.add(metadata.getStateStore());
                names.add(metadata.getOutput());
            }
        }

        return names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Topic topic = (Topic) o;
        return Objects.equals(inputTopic, topic.inputTopic)
                && Objects.equals(inputKey, topic.inputKey)
                && Objects.equals(inputValue, topic.inputValue)
                && Objects.equals(aggregator, topic.aggregator)
                && Objects.equals(baseOutput, topic.baseOutput)
                && Objects.equals(output, topic.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputTopic, inputKey, inputValue, aggregator, baseOutput, output);
    }

    public String toString(boolean reduced) {
        StringBuilder result = new StringBuilder()
                .append("inputTopic: ").append(inputTopic).append('\n')
                .append("inputKey: ").append(inputKey).append('\n')
                .append("inputValue: ").append(inputValue).append('\n')
                .append("aggregator: ").append(aggregator).append('\n');

        if (output.isEmpty()) {
            result.append("output: empty");
        } else {
            result.append("output:");
        }
        if (reduced) {
            output.forEach(topic -> result.append("\n\t- ").append(topic.getOutput()));
        } else {
            output.forEach(topic -> result
                    .append("\n\t- ").append(topic.getInput())
                    .append('\t').append(topic.getStateStore())
                    .append('\t').append(topic.getOutput()));
        }

        return result.append('\n').toString();
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static String getOutTopic(String topicName) {
        return topicName + OUTPUT_LABEL;
    }

    /**
     * TODO.
     * @param topicName TODO
     * @param timeLabel TODO
     * @return TODO
     */
    public static String getOutTopic(String topicName, TimeLabel timeLabel) {
        return topicName + timeLabel.getLabel();
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    public static Set<String> getTimedOutTopics(String topicName) {
        Set<String> set = new HashSet<>();

        for (TimeLabel label : TimeLabel.values()) {
            if (label.getIntervalInMilliSec() != -1) {
                set.add(topicName + label.getLabel());
            }
        }

        return set;
    }

    /**
     * Kafka Streams allows for stateful stream processing. The internal state is managed in
     *      so-called state stores. A fault-tolerant state store is an internally created and
     *      compacted changelog topic. This function return the changelog topic name.
     *
     * @param inputTopic {@link String} stating the inputTopic topic name read by the stateful
     *      stream
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
}
