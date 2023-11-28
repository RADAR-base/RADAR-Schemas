package org.radarbase.schema.specification.monitor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.config.OpenConfig
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.MONITOR
import org.radarbase.schema.specification.AppDataTopic
import org.radarbase.schema.specification.AppSource

@JsonInclude(NON_NULL)
@OpenConfig
class MonitorSource : AppSource<AppDataTopic>() {
    @JsonProperty
    override val data: MutableList<AppDataTopic> = mutableListOf()
    override val scope: Scope = MONITOR
}
