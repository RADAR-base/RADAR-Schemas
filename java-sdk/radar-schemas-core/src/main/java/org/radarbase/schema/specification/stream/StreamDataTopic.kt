package org.radarbase.schema.specification.stream

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import org.radarbase.config.AvroTopicConfig
import org.radarbase.config.OpenConfig
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.specification.DataTopic
import org.radarbase.schema.util.SchemaUtils.applyOrEmpty
import org.radarbase.stream.TimeWindowMetadata
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.AggregateKey
import org.radarcns.kafka.ObservationKey
import java.util.Arrays
import java.util.stream.Stream

/**
 * Topic used for Kafka Streams.
 */
@JsonInclude(NON_NULL)
@OpenConfig
class StreamDataTopic : DataTopic() {
    /** Whether the stream is a windowed stream with standard TimeWindow windows.  */
    @JsonProperty
    @set:JsonSetter
    var windowed = false
        set(value) {
            field = value
            if (value && (keySchema == null || keySchema == ObservationKey::class.java.getName())) {
                keySchema = AggregateKey::class.java.getName()
            }
        }

    /** Input topic for the stream.  */
    @JsonProperty("input_topics")
    var inputTopics: MutableList<String> = mutableListOf()

    /**
     * Base topic name for output topics. If windowed, output topics would become
     * `[topicBase]_[time-frame]`, otherwise it becomes `[topicBase]_output`.
     * If a fixed topic is set, this will override the topic base for non-windowed topics.
     */
    @JsonProperty("topic_base")
    var topicBase: String? = null

    @JsonSetter("input_topic")
    private fun setInputTopic(inputTopic: String) {
        if (topicBase == null) {
            topicBase = inputTopic
        }
        check(inputTopics.isEmpty()) { "Input topics already set" }
        inputTopics.add(inputTopic)
    }

    override var topic: String? = null
        /** Get human readable output topic.  */
        get() = if (windowed) {
            "${topicBase}_<time-frame>"
        } else {
            field ?: "${topicBase}_output"
        }

    @get:JsonIgnore
    override val topicNames: Stream<String>
        get() = if (windowed) {
            Arrays.stream(TimeWindowMetadata.entries.toTypedArray())
                .map { label: TimeWindowMetadata -> label.getTopicLabel(topicBase) }
        } else {
            var currentTopic = topic
            if (currentTopic == null) {
                currentTopic = topicBase + "_output"
                topic = currentTopic
            }
            Stream.of(currentTopic)
        }

    @JsonIgnore
    override fun topics(schemaCatalogue: SchemaCatalogue): Stream<AvroTopic<*, *>> {
        return topicNames
            .flatMap(
                applyOrEmpty { topic ->
                    val config = AvroTopicConfig()
                    config.topic = topic
                    config.keySchema = keySchema
                    config.valueSchema = valueSchema
                    Stream.of(schemaCatalogue.genericAvroTopic(config))
                },
            )
    }

    @get:JsonIgnore
    val timedTopicNames: Stream<String>
        /** Get only topic names that are used with the fixed time windows.  */
        get() = if (windowed) {
            topicNames
        } else {
            Stream.empty()
        }

    override fun propertiesMap(map: MutableMap<String, Any?>, reduced: Boolean) {
        map["input_topics"] = inputTopics
        map["windowed"] = windowed
        if (!reduced) {
            map["topic_base"] = topicBase
        }
    }
}
