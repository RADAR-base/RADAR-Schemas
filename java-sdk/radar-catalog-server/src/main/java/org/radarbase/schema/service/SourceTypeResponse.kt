package org.radarbase.schema.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.schema.specification.active.ActiveSource
import org.radarbase.schema.specification.connector.ConnectorSource
import org.radarbase.schema.specification.monitor.MonitorSource
import org.radarbase.schema.specification.passive.PassiveSource
import org.radarbase.schema.specification.push.PushSource
import org.radarbase.schema.specification.stream.StreamGroup

/** Response with source types.  */
class SourceTypeResponse(
    @JsonProperty("passive-source-types")
    val passiveSources: List<PassiveSource>? = null,
    @JsonProperty("active-source-types")
    val activeSources: List<ActiveSource<*>>? = null,
    @JsonProperty("monitor-source-types")
    val monitorSources: List<MonitorSource>? = null,
    @JsonProperty("connector-source-types")
    val connectorSources: List<ConnectorSource>? = null,
    @JsonProperty("stream-source-types")
    val streamSources: List<StreamGroup>? = null,
    @JsonProperty("push-source-types")
    val pushSources: List<PushSource>? = null,
)
