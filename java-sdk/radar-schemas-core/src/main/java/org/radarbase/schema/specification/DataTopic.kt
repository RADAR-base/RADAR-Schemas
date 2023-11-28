package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER
import org.radarbase.config.AvroTopicConfig
import org.radarbase.config.OpenConfig
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.specification.AppDataTopic.DataField
import org.radarbase.schema.util.SchemaUtils
import org.radarbase.topic.AvroTopic
import org.radarcns.catalogue.Unit
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.stream.Stream

/** DataTopic topic from a data producer.  */
@JsonInclude(NON_NULL)
@OpenConfig
class DataTopic : AvroTopicConfig() {
    /** Type of topic. Its meaning is class-specific. */
    @JsonProperty
    val type: String? = null

    /** Documentation string for this topic.  */
    @JsonProperty
    val doc: String? = null

    /** Sampling rate, how frequently messages are expected to be sent on average.  */
    @JsonProperty("sample_rate")
    val sampleRate: SampleRateConfig? = null

    /** Output unit.  */
    @JsonProperty
    val unit: Unit? = null

    /** Record fields that the given unit applies to.  */
    @JsonProperty
    val fields: List<DataField>? = null

    @get:JsonIgnore
    val topicNames: Stream<String>
        /** Get all topic names that are provided by the data.  */
        get() = Stream.of(topic)

    /** Get all Avro topics that are provided by the data.  */
    @JsonIgnore
    @Throws(IOException::class)
    fun topics(schemaCatalogue: SchemaCatalogue): Stream<AvroTopic<*, *>> {
        return Stream.of(schemaCatalogue.genericAvroTopic(this))
    }

    @JsonProperty("key_schema")
    @set:JsonSetter
    override var keySchema: String? = ObservationKey::class.java.getName()
        set(schema) {
            field = SchemaUtils.expandClass(schema)
        }

    @JsonProperty("value_schema")
    @set:JsonSetter
    override var valueSchema: String? = null
        set(schema) {
            field = SchemaUtils.expandClass(schema)
        }

    override fun toString(): String {
        return toString(false)
    }

    /**
     * Convert the topic to String, either as dense string or as verbose YAML string.
     * @param prettyString Whether the result should be a verbose pretty-printed string.
     * @return topic as a string.
     */
    fun toString(prettyString: Boolean): String {
        val name = javaClass.getSimpleName()
        // preserves insertion order
        val properties: MutableMap<String, Any?> = LinkedHashMap()
        propertiesMap(properties, !prettyString)
        return if (prettyString) {
            val mapper = ObjectMapper(
                YAMLFactory().apply {
                    disable(WRITE_DOC_START_MARKER)
                    enable(MINIMIZE_QUOTES)
                },
            )
            try {
                mapper.writeValueAsString(mapOf(name to properties))
            } catch (ex: JsonProcessingException) {
                logger.error("Failed to convert data to YAML", ex)
                name + properties
            }
        } else {
            name + properties
        }
    }

    /**
     * Turns this topic into an descriptive properties map.
     * @param map properties to add to.
     * @param reduced whether to set a reduced set of properties, to decrease verbosity.
     */
    protected fun propertiesMap(map: MutableMap<String, Any?>, reduced: Boolean) {
        map["type"] = type
        if (!reduced && doc != null) {
            map["doc"] = doc
        }
        val topic: String? = topic
        if (topic != null) {
            map["topic"] = topic
        }
        map["key_schema"] = keySchema
        map["value_schema"] = valueSchema
        if (!reduced) {
            if (sampleRate != null) {
                map["sample_rate"] = sampleRate
            }
            if (unit != null) {
                map["unit"] = unit
            }
            if (fields != null) {
                map["fields"] = fields
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataTopic::class.java)
    }
}
