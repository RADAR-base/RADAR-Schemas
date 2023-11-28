package org.radarbase.schema.specification.stream

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotEmpty
import org.radarbase.config.OpenConfig
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.STREAM
import org.radarbase.schema.specification.DataProducer
import java.util.stream.Stream

/**
 * Data producer for Kafka Streams. This data topic does not register schemas to the schema registry
 * by default, since Kafka Streams will do that itself. To disable this, set the
 * `register_schema` property to `true`.
 */
@JsonInclude(NON_NULL)
@OpenConfig
class StreamGroup : DataProducer<StreamDataTopic>() {
    @JsonProperty
    @NotEmpty
    override val data: MutableList<StreamDataTopic> = mutableListOf()

    @JsonProperty
    val master: String? = null

    override var registerSchema: Boolean = false

    override val scope: Scope = STREAM

    @get:JsonIgnore
    val timedTopicNames: Stream<String>
        /** Get only the topic names that are the output of a timed stream.  */
        get() = data.stream()
            .flatMap { it.timedTopicNames }
}
