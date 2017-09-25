package org.radarcns.schema.specification.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.catalogue.Unit;
import org.radarcns.config.AvroTopicConfig;
import org.radarcns.kafka.MeasurementKey;
import org.radarcns.kafka.WindowedKey;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.topic.AvroTopic;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.radarcns.schema.util.Utils.applyOrEmpty;

public class StreamDataTopic extends DataTopic {

    public enum TimeLabel {
        TEN_SECOND(TimeWindow.TEN_SECOND, TimeUnit.SECONDS.toMillis(10), "_10sec"),
        ONE_MIN(TimeWindow.ONE_MIN, TimeUnit.MINUTES.toMillis(1), "_1min"),
        TEN_MIN(TimeWindow.TEN_MIN, TimeUnit.MINUTES.toMillis(10), "_10min"),
        ONE_HOUR(TimeWindow.ONE_HOUR, TimeUnit.HOURS.toMillis(1), "_1hour"),
        ONE_DAY(TimeWindow.ONE_DAY, TimeUnit.DAYS.toMillis(1), "_1day"),
        ONE_WEEK(TimeWindow.ONE_WEEK, TimeUnit.DAYS.toMillis(7), "_1week");

        private final TimeWindow timeWindow;
        private final long intervalInMilliSec;
        private final String label;

        TimeLabel(TimeWindow timeWindow, long intervalInMilliSec, String label) {
            this.timeWindow = timeWindow;
            this.intervalInMilliSec = intervalInMilliSec;
            this.label = label;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        public long getIntervalInMilliSec() {
            return intervalInMilliSec;
        }

        public String getLabel(String topic) {
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

    @JsonProperty("topic_base")
    private String topicBase;

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setWindowed(boolean windowed) {
        this.windowed = windowed;
        if (windowed && (this.getKeySchema() == null
                || this.getKeySchema().equals(MeasurementKey.class.getName()))) {
            this.setKeySchema(WindowedKey.class.getName());
        }
    }

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setInputTopic(String inputTopic) {
        if (topicBase == null) {
            topicBase = inputTopic;
        }
        this.inputTopic = inputTopic;
    }

    public String getTopic() {
        if (windowed) {
            return topicBase + "_<time-frame>";
        } else {
            return topicBase + "_output";
        }
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

    public String getTopicBase() {
        return topicBase;
    }

    public Stream<String> getTopicNames() {
        if (windowed) {
            return Arrays.stream(TimeLabel.values())
                    .map(label -> label.getLabel(topicBase));
        } else {
            String currentTopic = getTopic();
            if (currentTopic == null) {
                currentTopic = topicBase + "_output";
                setTopic(currentTopic);
            }
            return Stream.of(currentTopic);
        }
    }

    public Stream<AvroTopic<?, ?>> getTopics() throws IOException {
        return getTopicNames()
                .flatMap(applyOrEmpty(topic -> {
                    AvroTopicConfig config = new AvroTopicConfig();
                    config.setTopic(topic);
                    config.setKeySchema(getKeySchema());
                    config.setValueSchema(getValueSchema());
                    return Stream.of(config.parseAvroTopic());
                }));
    }

    public Stream<String> getTimedTopicNames() {
        if (windowed) {
            return getTopicNames();
        } else {
            return Stream.empty();
        }
    }

    @Override
    protected void propertiesMap(Map<String, Object> properties, boolean reduce) {
        properties.put("input_topic", inputTopic);
        properties.put("windowed", windowed);
        if (!reduce) {
            if (!topicBase.equals(inputTopic)) {
                properties.put("topic_base", topicBase);
            }
            if (unit != null) {
                properties.put("unit", unit);
            }
        }
    }
}
