package org.radarbase.schema.specification.connector

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.config.OpenConfig
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.CONNECTOR
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.DataTopic

/**
 * Data producer for third-party connectors. This data topic does not register schemas to the schema
 * registry by default, since Kafka Connect will do that itself. To enable auto-registration, set
 * the `register_schema` property to `true`.
 */
@JsonInclude(NON_NULL)
@OpenConfig
class ConnectorSource : DataProducer<DataTopic>() {
    @JsonProperty
    override var data: MutableList<DataTopic> = mutableListOf()

    @JsonProperty
    var vendor: String? = null

    @JsonProperty
    var model: String? = null

    @JsonProperty
    var version: String? = null

    override var registerSchema = false

    override val scope: Scope = CONNECTOR
}
