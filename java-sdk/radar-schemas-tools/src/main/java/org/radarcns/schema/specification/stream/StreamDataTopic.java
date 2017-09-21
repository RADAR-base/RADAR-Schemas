package org.radarcns.schema.specification.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.radarcns.catalogue.TimeFrame;
import org.radarcns.catalogue.Unit;
import org.radarcns.kafka.key.KeyMeasurement;
import org.radarcns.kafka.key.KeyWindowed;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.topic.AvroTopic;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.radarcns.schema.util.Utils.applyOrEmpty;

public class StreamDataTopic extends DataTopic {

    public enum TimeLabel {
        TEN_SECOND(TimeFrame.TEN_SECOND, TimeUnit.SECONDS.toMillis(10), "_10sec"),
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

        public String getTimedTopic(String topic) {
            return topic + label;
        }
    }

    @JsonProperty
    private boolean windowed = false;

    @JsonProperty
    private Unit unit;

    @JsonProperty("input_topic")
    @NotBlank
    private String inputTopic;

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setWindowed(boolean windowed) {
        this.windowed = windowed;
        if (windowed && (this.getKeySchema() == null
                || this.getKeySchema().equals(KeyMeasurement.class.getName()))) {
            this.setKeySchema(KeyWindowed.class.getName());
        }
    }

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setInputTopic(String inputTopic) {
        if (getTopic() == null) {
            setTopic(inputTopic + "_output");
        }
        this.inputTopic = inputTopic;
    }

    public boolean isWindowed() {
        return windowed;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public Unit getUnit() {
        return unit;
    }

    public Stream<String> getTopicNames() {
        if (windowed) {
            return Arrays.stream(TimeLabel.values())
                .map(label -> label.getTimedTopic(inputTopic))
                .flatMap(topic -> Stream.of(topic, "From-" + inputTopic + "-To-" + topic));
        } else {
            return Stream.of(getTopic(), "From-" + inputTopic + "-To-" + getTopic());
        }
    }

    public Stream<AvroTopic<?, ?>> getTopics() throws IOException {
        if (windowed) {
            return Arrays.stream(TimeLabel.values())
                    .map(label -> label.getTimedTopic(inputTopic))
                    .flatMap(applyOrEmpty(topic -> {
                        setTopic(topic);
                        return super.getTopics();
                    }));
        } else {
            return super.getTopics();
        }
    }

    public Stream<String> getTimedTopicNames() {
        if (windowed) {
            return Arrays.stream(TimeLabel.values())
                    .map(label -> label.getTimedTopic(inputTopic))
                    .flatMap(topic -> Stream.of(topic, "From-" + inputTopic + "-To-" + topic));
        } else {
            return Stream.empty();
        }
    }
}
