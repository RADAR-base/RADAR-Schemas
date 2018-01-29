package org.radarcns.schema.specification.stream;

import static org.radarcns.schema.util.Utils.applyOrEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.radarcns.config.AvroTopicConfig;
import org.radarcns.kafka.AggregateKey;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.stream.TimeWindowMetadata;
import org.radarcns.topic.AvroTopic;

public class StreamDataTopic extends DataTopic {

    /** Whether the stream is a windowed stream with standard TimeWindow windows. */
    @JsonProperty
    private boolean windowed = false;

    /** Input topic for the stream. */
    @JsonProperty("input_topics")
    private final List<String> inputTopics = new ArrayList<>();

    /**
     * Base topic name for output topics. If windowed, output topics would become
     * {@code [topicBase]_[time-frame]}, otherwise it becomes {@code [topicBase]_output}.
     * If a fixed topic is set, this will override the topic base for non-windowed topics.
     */
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

    @JsonSetter("input_topic")
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setInputTopic(String inputTopic) {
        if (topicBase == null) {
            topicBase = inputTopic;
        }
        if (!this.inputTopics.isEmpty()) {
            throw new IllegalStateException("Input topics already set");
        }
        this.inputTopics.add(inputTopic);
    }

    /** Get human readable output topic. */
    public String getTopic() {
        if (windowed) {
            return topicBase + "_<time-frame>";
        } else if (super.getTopic() != null) {
            return super.getTopic();
        } else {
            return topicBase + "_output";
        }
    }

    public boolean isWindowed() {
        return windowed;
    }

    /** Get the input topics. */
    public List<String> getInputTopics() {
        return inputTopics;
    }

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setInputTopics(Collection<? extends String> topics) {
        if (!this.inputTopics.isEmpty()) {
            throw new IllegalStateException("Input topics already set");
        }
        this.inputTopics.addAll(topics);
    }

    public String getTopicBase() {
        return topicBase;
    }

    @Override
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

    @Override
    public Stream<AvroTopic<?, ?>> getTopics() {
        return getTopicNames()
                .flatMap(applyOrEmpty(topic -> {
                    AvroTopicConfig config = new AvroTopicConfig();
                    config.setTopic(topic);
                    config.setKeySchema(getKeySchema());
                    config.setValueSchema(getValueSchema());
                    return Stream.of(config.parseAvroTopic());
                }));
    }

    /** Get only topic names that are used with the fixed time windows. */
    public Stream<String> getTimedTopicNames() {
        if (windowed) {
            return getTopicNames();
        } else {
            return Stream.empty();
        }
    }

    @Override
    protected void propertiesMap(Map<String, Object> properties, boolean reduce) {
        properties.put("input_topics", inputTopics);
        properties.put("windowed", windowed);
        if (!reduce) {
            properties.put("topic_base", topicBase);
        }
    }
}
