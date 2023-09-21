package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.radarbase.config.OpenConfig
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.Scope
import org.radarbase.schema.util.SchemaUtils.applyOrEmpty
import org.radarbase.topic.AvroTopic
import java.util.Objects
import java.util.stream.Stream

/**
 * A producer of data to Kafka, generally mapping to a source.
 * @param <T> type of data that is produced.
</T> */
@JsonInclude(NON_NULL)
@OpenConfig
abstract class DataProducer<T : DataTopic> {
    @JsonProperty
    var name: @NotBlank String? = null

    @JsonProperty
    var doc: @NotBlank String? = null

    @JsonProperty
    var properties: Map<String, String>? = null

    @JsonProperty
    var labels: Map<String, String>? = null

    /**
     * If true, register the schema during kafka initialization, otherwise, the producer should do
     * that itself. The default is true, set in the constructor of subclasses to use a different
     * default.
     */
    @JsonProperty("register_schema")
    var registerSchema = true

    abstract val data: @NotNull MutableList<T>
    abstract val scope: @NotNull Scope?

    @get:JsonIgnore
    val topicNames: Stream<String>
        get() = data.stream().flatMap(DataTopic::topicNames)

    @JsonIgnore
    fun topics(schemaCatalogue: SchemaCatalogue): Stream<AvroTopic<*, *>> =
        data.stream().flatMap(
            applyOrEmpty { t ->
                t.topics(schemaCatalogue)
            },
        )

    fun doRegisterSchema(): Boolean {
        return registerSchema
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as DataProducer<*>
        return name == other.name &&
            doc == other.doc &&
            data == other.data
    }

    override fun hashCode(): Int {
        return Objects.hash(name, doc, data)
    }
}
