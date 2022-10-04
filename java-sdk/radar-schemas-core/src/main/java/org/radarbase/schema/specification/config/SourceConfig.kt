package org.radarbase.schema.specification.config

import org.radarbase.schema.specification.active.ActiveSource
import org.radarbase.schema.specification.connector.ConnectorSource
import org.radarbase.schema.specification.monitor.MonitorSource
import org.radarbase.schema.specification.passive.PassiveSource
import org.radarbase.schema.specification.push.PushSource
import org.radarbase.schema.specification.stream.StreamGroup
import java.nio.file.FileSystem
import java.nio.file.PathMatcher

data class SourceConfig(
    val include: List<String> = listOf(),
    val exclude: List<String> = listOf(),
    val active: List<ActiveSource<*>> = emptyList(),
    val connector: List<ConnectorSource> = emptyList(),
    val monitor: List<MonitorSource> = emptyList(),
    val passive: List<PassiveSource> = emptyList(),
    val push: List<PushSource> = emptyList(),
    val stream: List<StreamGroup> = emptyList(),
) {
    fun pathMatcher(fs: FileSystem): PathMatcher = when {
        include.isNotEmpty() -> {
            val matchers = include.map { fs.getPathMatcher("glob:$it") }
            PathMatcher { path -> matchers.any { it.matches(path) } }
        }
        exclude.isNotEmpty() -> {
            val matchers = exclude.map { fs.getPathMatcher("glob:$it") }
            PathMatcher { path -> matchers.none { it.matches(path) } }
        }
        else -> PathMatcher { true }
    }
}
