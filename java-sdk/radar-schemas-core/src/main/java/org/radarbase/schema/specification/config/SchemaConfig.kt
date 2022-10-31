package org.radarbase.schema.specification.config

import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.*

data class SchemaConfig(
    override val include: List<String> = listOf(),
    override val exclude: List<String> = listOf(),
    val active: Map<String, String> = emptyMap(),
    val kafka: Map<String, String> = emptyMap(),
    val catalogue: Map<String, String> = emptyMap(),
    val connector: Map<String, String> = emptyMap(),
    val monitor: Map<String, String> = emptyMap(),
    val passive: Map<String, String> = emptyMap(),
    val push: Map<String, String> = emptyMap(),
    val stream: Map<String, String> = emptyMap(),
) : PathMatcherConfig {
    fun schemas(scope: Scope): Map<String, String> = when(scope) {
        ACTIVE -> active
        KAFKA -> kafka
        CATALOGUE -> catalogue
        MONITOR -> monitor
        PASSIVE -> passive
        STREAM -> stream
        CONNECTOR -> connector
        PUSH -> push
    }
}
