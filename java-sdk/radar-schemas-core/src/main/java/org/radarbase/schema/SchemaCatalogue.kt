package org.radarbase.schema

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.radarbase.config.AvroTopicConfig
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.SchemaValidator
import org.radarbase.schema.validation.rules.SchemaMetadata
import org.radarbase.topic.AvroTopic
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.io.path.exists
import kotlin.io.path.inputStream

class SchemaCatalogue @JvmOverloads constructor(
    private val schemaRoot: Path,
    config: SchemaConfig,
    scope: Scope? = null,
) {
    val schemas: Map<String, SchemaMetadata>
    val unmappedAvroFiles: List<SchemaMetadata>

    init {
        val schemaTemp = HashMap<String, SchemaMetadata>()
        val unmappedTemp = mutableListOf<SchemaMetadata>()
        val matcher = config.pathMatcher(schemaRoot)
        if (scope != null) {
            loadSchemas(schemaTemp, unmappedTemp, scope, matcher, config)
        } else {
            for (useScope in Scope.entries) {
                loadSchemas(schemaTemp, unmappedTemp, useScope, matcher, config)
            }
        }
        schemas = schemaTemp.toMap()
        unmappedAvroFiles = unmappedTemp.toList()
    }

    /**
     * Returns an avro topic with the schemas from this catalogue.
     * @param config avro topic configuration
     * @return AvroTopic with
     * @throws NoSuchElementException if the key or value schema do not exist in this catalogue.
     * @throws NullPointerException if the key or value schema configurations are null
     * @throws IllegalArgumentException if the topic configuration is null
     */
    fun getGenericAvroTopic(config: AvroTopicConfig): AvroTopic<GenericRecord, GenericRecord> {
        val (keySchema, valueSchema) = getSchemaMetadata(config)
        return AvroTopic(
            requireNotNull(config.topic) { "Missing Avro topic in configuration" },
            requireNotNull(keySchema.schema) { "Missing Avro key schema" },
            requireNotNull(valueSchema.schema) { "Missing Avro value schema" },
            GenericRecord::class.java,
            GenericRecord::class.java,
        )
    }

    @Throws(IOException::class)
    private fun loadSchemas(
        schemas: MutableMap<String, SchemaMetadata>,
        unmappedFiles: MutableList<SchemaMetadata>,
        scope: Scope,
        matcher: PathMatcher,
        config: SchemaConfig,
    ) {
        val walkRoot = schemaRoot.resolve(scope.lower)
        val avroFiles = buildMap<Path, String> {
            if (walkRoot.exists()) {
                Files.walk(walkRoot).use { walker ->
                    walker
                        .filter { p ->
                            matcher.matches(p) && SchemaValidator.isAvscFile(p)
                        }
                        .forEach { p ->
                            p.inputStream().reader().use {
                                put(p, it.readText())
                            }
                        }
                }
            }
            config.schemas(scope).forEach { (key, value) ->
                put(walkRoot.resolve(key), value)
            }
        }

        var prevSize = -1

        // Recursively parse all schemas.
        // If the parsed schema size does not change anymore, the final schemas cannot be parsed
        // at all.
        while (prevSize != schemas.size) {
            prevSize = schemas.size
            val useTypes = schemas.toSchemaMap()
            val ignoreFiles = schemas.values.asSequence()
                .map { it.path }
                .filterNotNullTo(HashSet())

            schemas.putParsedSchemas(avroFiles, ignoreFiles, useTypes, scope)
        }
        val mappedPaths = schemas.values.mapTo(HashSet()) { it.path }

        avroFiles.keys.asSequence()
            .filter { it !in mappedPaths }
            .distinct()
            .mapTo(unmappedFiles) { p -> SchemaMetadata(null, scope, p) }
    }

    private fun MutableMap<String, SchemaMetadata>.putParsedSchemas(
        customSchemas: Map<Path, String>,
        ignoreFiles: Set<Path>,
        useTypes: Map<String, Schema>,
        scope: Scope,
    ) = customSchemas.asSequence()
        .filter { (p, _) -> p !in ignoreFiles }
        .forEach { (p, schema) ->
            val parser = Schema.Parser()
            parser.addTypes(useTypes)
            try {
                val parsedSchema = parser.parse(schema)
                put(parsedSchema.fullName, SchemaMetadata(parsedSchema, scope, p))
            } catch (ex: Exception) {
                logger.debug("Cannot parse schema {}: {}", p, ex.toString())
            }
        }

    /**
     * Returns an avro topic with the schemas from this catalogue.
     * @param config avro topic configuration
     * @return AvroTopic with
     * @throws NoSuchElementException if the key or value schema do not exist in this catalogue.
     * @throws NullPointerException if the key or value schema configurations are null
     * @throws IllegalArgumentException if the topic configuration is null
     */
    fun getSchemaMetadata(config: AvroTopicConfig): Pair<SchemaMetadata, SchemaMetadata> {
        val parsedKeySchema = schemas[config.keySchema]
            ?: throw NoSuchElementException(
                "Key schema " + config.keySchema +
                    " for topic " + config.topic + " not found.",
            )
        val parsedValueSchema = schemas[config.valueSchema]
            ?: throw NoSuchElementException(
                "Value schema " + config.valueSchema +
                    " for topic " + config.topic + " not found.",
            )
        return Pair(parsedKeySchema, parsedValueSchema)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaCatalogue::class.java)

        fun Map<String, SchemaMetadata>.toSchemaMap(): Map<String, Schema> = buildMap(size) {
            this@toSchemaMap.forEach { (k, v) ->
                if (v.schema != null) {
                    put(k, v.schema)
                }
            }
        }
    }
}
