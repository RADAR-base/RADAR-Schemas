package org.radarcns.schema.specification.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.radarcns.catalogue.Unit;
import org.radarcns.config.AvroTopicConfig;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.kafka.AggregateKey;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.stream.TimeWindowMetadata;
import org.radarcns.topic.AvroTopic;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.radarcns.schema.util.Utils.applyOrEmpty;

public class StreamDataTopic extends DataTopic {

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
                || this.getKeySchema().equals(ObservationKey.class.getName()))) {
            this.setKeySchema(AggregateKey.class.getName());
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
            return Arrays.stream(TimeWindowMetadata.values())
                    .map(label -> label.getTopicLabel(topicBase));
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
