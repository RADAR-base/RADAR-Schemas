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
import kotlin.collections.HashSet
import kotlin.io.path.exists
import kotlin.io.path.readText

class SchemaCatalogue(
    val schemas: Map<String, SchemaMetadata>,
    val unmappedSchemas: List<FailedSchemaMetadata>,
) {
    /**
     * Returns an avro topic with the schemas from this catalogue.
     * @param config avro topic configuration
     * @return AvroTopic with
     * @throws NoSuchElementException if the key or value schema do not exist in this catalogue.
     * @throws NullPointerException if the key or value schema configurations are null
     * @throws IllegalArgumentException if the topic configuration is null
     */
    fun genericAvroTopic(config: AvroTopicConfig): AvroTopic<GenericRecord, GenericRecord> {
        val (keySchema, valueSchema) = topicSchemas(config)
        return AvroTopic(
            requireNotNull(config.topic) { "Missing Avro topic in configuration" },
            requireNotNull(keySchema.schema) { "Missing Avro key schema" },
            requireNotNull(valueSchema.schema) { "Missing Avro value schema" },
            GenericRecord::class.java,
            GenericRecord::class.java,
        )
    }

    /**
     * Returns an avro topic with the schemas from this catalogue.
     * @param config avro topic configuration
     * @return AvroTopic with
     * @throws NoSuchElementException if the key or value schema do not exist in this catalogue.
     * @throws NullPointerException if the key or value schema configurations are null
     * @throws IllegalArgumentException if the topic configuration is null
     */
    fun topicSchemas(config: AvroTopicConfig): Pair<SchemaMetadata, SchemaMetadata> {
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
}

private val logger = LoggerFactory.getLogger(SchemaCatalogue::class.java)

/**
 * Load a schema catalogue.
 * @param schemaRoot root of schema directory.
 * @param config schema configuration
 * @param scope scope to read. If null, all scopes are read.
 */
suspend fun SchemaCatalogue(
    schemaRoot: Path,
    config: SchemaConfig,
    scope: Scope? = null,
): SchemaCatalogue {
    val matcher = config.pathMatcher(schemaRoot)
    val (schemas, unmapped) = runBlocking {
        if (scope != null) {
            loadSchemas(schemaRoot, scope, matcher, config)
        } else {
            Scope.entries
                .forkJoin { s -> loadSchemas(schemaRoot, s, matcher, config) }
                .reduce { (m1, l1), (m2, l2) -> Pair(m1 + m2, l1 + l2) }
        }
    }
    return SchemaCatalogue(schemas, unmapped)
}

@Throws(IOException::class)
private suspend fun loadSchemas(
    schemaRoot: Path,
    scope: Scope,
    matcher: PathMatcher,
    config: SchemaConfig,
): Pair<Map<String, SchemaMetadata>, List<FailedSchemaMetadata>> {
    val scopeRoot = schemaRoot.resolve(scope.lower)
    val avroFiles = buildMap<Path, String> {
        if (scopeRoot.exists()) {
            scopeRoot
                .listRecursive { matcher.matches(it) && it.isAvscFile() }
                .forkJoin(Dispatchers.IO) { p ->
                    p to p.readText()
                }
                .toMap(this@buildMap)
        }
        config.schemas(scope).forEach { (key, value) ->
            put(scopeRoot.resolve(key), value)
        }
    }

    var prevSize = -1

    // Recursively parse all schemas.
    // If the parsed schema size does not change anymore, the final schemas cannot be parsed
    // at all.
    val schemas = buildMap<String, SchemaMetadata> {
        while (prevSize != size) {
            prevSize = size
            val useTypes = mapValues { (_, v) -> v.schema }
            val ignoreFiles = values.mapTo(HashSet()) { it.path }

            putAll(avroFiles.parseSchemas(ignoreFiles, useTypes, scope))
        }
    }
    val mappedPaths = schemas.values.mapTo(HashSet()) { it.path }

    val unmapped = avroFiles.keys
        .filterTo(HashSet()) { it !in mappedPaths }
        .map { p -> FailedSchemaMetadata(scope, p) }

    return Pair(schemas, unmapped)
}

private suspend fun Map<Path, String>.parseSchemas(
    ignoreFiles: Set<Path>,
    useTypes: Map<String, Schema>,
    scope: Scope,
) = filter { (p, _) -> p !in ignoreFiles }
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
    .toMap()
