package org.radarbase.schema.specification.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.IOException
import java.nio.file.Paths
import kotlin.io.path.inputStream

data class ToolConfig(
    val kafka: Map<String, Any> = emptyMap(),
    val topics: Map<String, TopicConfig> = emptyMap(),
    val schemas: SchemaConfig = SchemaConfig(),
    val sources: SourceConfig = SourceConfig(),
)

@Throws(IOException::class)
fun loadToolConfig(fileName: String?): ToolConfig {
    if (fileName.isNullOrEmpty()) {
        return ToolConfig()
    }

    val mapper = ObjectMapper(YAMLFactory.builder().build())
        .registerModule(
            kotlinModule {
                enable(KotlinFeature.NullIsSameAsDefault)
            },
        )

    return Paths.get(fileName).inputStream().use { stream ->
        mapper.readValue(stream, ToolConfig::class.java)
    } ?: ToolConfig()
}
