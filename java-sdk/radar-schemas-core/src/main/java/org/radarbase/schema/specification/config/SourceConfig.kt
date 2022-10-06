package org.radarbase.schema.specification.config

import org.radarbase.schema.specification.active.ActiveSource
import org.radarbase.schema.specification.connector.ConnectorSource
import org.radarbase.schema.specification.monitor.MonitorSource
import org.radarbase.schema.specification.passive.PassiveSource
import org.radarbase.schema.specification.push.PushSource
import org.radarbase.schema.specification.stream.StreamGroup

data class SourceConfig(
    override val include: List<String> = listOf(),
    override val exclude: List<String> = listOf(),
    val active: List<ActiveSource<*>> = emptyList(),
    val connector: List<ConnectorSource> = emptyList(),
    val monitor: List<MonitorSource> = emptyList(),
    val passive: List<PassiveSource> = emptyList(),
    val push: List<PushSource> = emptyList(),
    val stream: List<StreamGroup> = emptyList(),
) : PathMatcherConfig
