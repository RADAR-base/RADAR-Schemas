package org.radarbase.schema

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.radarbase.config.AvroTopicConfig
import org.radarbase.kotlin.coroutines.forkJoin
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.util.SchemaUtils.listRecursive
import org.radarbase.schema.validation.SchemaValidator.Companion.isAvscFile
import org.radarbase.schema.validation.rules.FailedSchemaMetadata
import org.radarbase.schema.validation.rules.SchemaMetadata
import org.radarbase.topic.AvroTopic
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream

class SchemaCatalogue @JvmOverloads constructor(
    private val schemaRoot: Path,
    config: SchemaConfig,
    scope: Scope? = null,
) {
    val schemas: Map<String, SchemaMetadata>
    val unmappedAvroFiles: List<FailedSchemaMetadata>

    init {
        val schemaTemp = HashMap<String, SchemaMetadata>()
        val unmappedTemp = mutableListOf<FailedSchemaMetadata>()
        val matcher = config.pathMatcher(schemaRoot)
        runBlocking {
            if (scope != null) {
                loadSchemas(schemaTemp, unmappedTemp, scope, matcher, config)
            } else {
                for (useScope in Scope.entries) {
                    loadSchemas(schemaTemp, unmappedTemp, useScope, matcher, config)
                }
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
    private suspend fun loadSchemas(
        schemas: MutableMap<String, SchemaMetadata>,
        unmappedFiles: MutableList<FailedSchemaMetadata>,
        scope: Scope,
        matcher: PathMatcher,
        config: SchemaConfig,
    ) {
        val walkRoot = schemaRoot.resolve(scope.lower)
        val avroFiles = buildMap<Path, String> {
            if (walkRoot.exists()) {
                walkRoot
                    .listRecursive { matcher.matches(it) && it.isAvscFile() }
                    .forkJoin(Dispatchers.IO) { p ->
                        p.inputStream().reader().use {
                            p to it.readText()
                        }
                    }
                    .toMap(this@buildMap)
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
            val useTypes = schemas.mapValues { (_, v) -> v.schema }
            val ignoreFiles = schemas.values.mapTo(HashSet()) { it.path }

            schemas.putParsedSchemas(avroFiles, ignoreFiles, useTypes, scope)
        }
        val mappedPaths = schemas.values.mapTo(HashSet()) { it.path }

        avroFiles.keys.asSequence()
            .filter { it !in mappedPaths }
            .distinct()
            .mapTo(unmappedFiles) { p -> FailedSchemaMetadata(scope, p) }
    }

    private suspend fun MutableMap<String, SchemaMetadata>.putParsedSchemas(
        customSchemas: Map<Path, String>,
        ignoreFiles: Set<Path>,
        useTypes: Map<String, Schema>,
        scope: Scope,
    ) = customSchemas
        .filter { (p, _) -> p !in ignoreFiles }
        .entries
        .forkJoin { (p, schema) ->
            val parser = Schema.Parser()
            parser.addTypes(useTypes)
            withContext(Dispatchers.IO) {
                try {
                    val parsedSchema = parser.parse(schema)
                    parsedSchema.fullName to SchemaMetadata(parsedSchema, scope, p)
                } catch (ex: Exception) {
                    logger.debug("Cannot parse schema {}: {}", p, ex.toString())
                    null
                }
            }
        }
        .filterNotNull()
        .toMap(this@putParsedSchemas)

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
    }
}
