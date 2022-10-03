package org.radarbase.schema.registration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.radarbase.schema.validation.config.ExcludeConfig
import java.io.IOException
import java.nio.file.Paths
import kotlin.io.path.bufferedReader

data class ToolConfig(
    val kafka: Map<String, Any> = emptyMap(),
    val topics: Map<String, TopicConfig> = emptyMap(),
    val exclude: ExcludeConfig = ExcludeConfig(),
)

@Throws(IOException::class)
fun loadToolConfig(fileName: String?): ToolConfig {
    if (fileName.isNullOrEmpty()) {
        return ToolConfig()
    }

    val mapper = ObjectMapper(YAMLFactory.builder().build())
        .registerModule(kotlinModule {
            enable(KotlinFeature.NullIsSameAsDefault)
        })

    return Paths.get(fileName).bufferedReader().use { stream ->
        mapper.readValue(stream, ToolConfig::class.java)
    } ?: ToolConfig()
}
