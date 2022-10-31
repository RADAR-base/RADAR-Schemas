package org.radarbase.schema.specification.config

data class TopicConfig(
    val enabled: Boolean = true,
    val partitions: Int? = null,
    val replicationFactor: Short? = null,
    val keySchema: String? = null,
    val valueSchema: String? = null,
    val properties: Map<String, String> = emptyMap(),
    val registerSchema: Boolean = true,
)
