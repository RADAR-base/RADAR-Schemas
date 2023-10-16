package org.radarbase.schema.specification.push

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.config.OpenConfig
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.PUSH
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.DataTopic

@JsonInclude(NON_NULL)
@OpenConfig
class PushSource : DataProducer<DataTopic>() {
    @JsonProperty
    override var data: MutableList<DataTopic> = mutableListOf()

    @JsonProperty
    var vendor: String? = null

    @JsonProperty
    var model: String? = null

    @JsonProperty
    var version: String? = null

    override val scope: Scope = PUSH
}
