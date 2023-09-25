/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.schema.validation

import org.apache.avro.Schema
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.Scope
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.rules.RadarSchemaMetadataRules
import org.radarbase.schema.validation.rules.RadarSchemaRules
import org.radarbase.schema.validation.rules.SchemaMetadata
import org.radarbase.schema.validation.rules.SchemaMetadataRules
import org.radarbase.schema.validation.rules.Validator
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.io.path.extension

/**
 * Validator for a set of RADAR-Schemas.
 *
 * @param schemaRoot RADAR-Schemas commons directory.
 * @param config configuration to exclude certain schemas or fields from validation.
 */
class SchemaValidator(schemaRoot: Path, config: SchemaConfig) {
    val rules: SchemaMetadataRules = RadarSchemaMetadataRules(schemaRoot, config)
    private val pathMatcher: PathMatcher = config.pathMatcher(schemaRoot)

    suspend fun analyseSourceCatalogue(
        scope: Scope?,
        catalogue: SourceCatalogue,
    ): List<ValidationException> {
        val validator = rules.isSchemaMetadataValid(true)
        val producers: Stream<DataProducer<*>> = if (scope != null) {
            catalogue.sources.stream()
                .filter { it.scope == scope }
        } else {
            catalogue.sources.stream()
        }
        return validationContext {
            val schemas = producers
                .flatMap { it.data.stream() }
                .flatMap { topic ->
                    val (keySchema, valueSchema) = catalogue.schemaCatalogue.getSchemaMetadata(topic)
                    Stream.of(keySchema, valueSchema)
                }
                .filter { it.schema != null }
                .sorted(Comparator.comparing { it.schema!!.fullName })
                .collect(Collectors.toSet())

            schemas.forEach { metadata ->
                if (pathMatcher.matches(metadata.path)) {
                    validator.launchValidation(metadata)
                }
            }
        }
    }

    suspend fun analyseFiles(
        schemaCatalogue: SchemaCatalogue,
        scope: Scope? = null,
    ): List<ValidationException> = validationContext {
        if (scope == null) {
            Scope.entries.forEach { scope -> analyseFilesInternal(schemaCatalogue, scope) }
        } else {
            analyseFilesInternal(schemaCatalogue, scope)
        }
    }

    private fun ValidationContext.analyseFilesInternal(
        schemaCatalogue: SchemaCatalogue,
        scope: Scope,
    ) {
        val validator = rules.isSchemaMetadataValid(false)
        val parsingValidator = parsingValidator(scope, schemaCatalogue)

        schemaCatalogue.unmappedAvroFiles.forEach { metadata ->
            parsingValidator.launchValidation(metadata)
        }

        schemaCatalogue.schemas.values.forEach { metadata ->
            if (pathMatcher.matches(metadata.path)) {
                validator.launchValidation(metadata)
            }
        }
    }

    private fun parsingValidator(
        scope: Scope?,
        schemaCatalogue: SchemaCatalogue,
    ): Validator<SchemaMetadata> {
        val useTypes = buildMap {
            schemaCatalogue.schemas.forEach { (key, value) ->
                if (value.scope == scope) {
                    put(key, value.schema)
                }
            }
        }
        return Validator { metadata ->
            val parser = Schema.Parser()
            parser.addTypes(useTypes)
            try {
                parser.parse(metadata.path?.toFile())
            } catch (ex: Exception) {
                raise("Cannot parse schema", ex)
            }
        }
    }

    /** Validate a single schema in given path.  */
    fun ValidationContext.validate(schema: Schema, path: Path, scope: Scope) =
        validate(SchemaMetadata(schema, scope, path))

    /** Validate a single schema in given path.  */
    private fun ValidationContext.validate(schemaMetadata: SchemaMetadata) {
        val validator = rules.isSchemaMetadataValid(false)
        if (pathMatcher.matches(schemaMetadata.path)) {
            validator.launchValidation(schemaMetadata)
        }
    }

    val validatedSchemas: Map<String, Schema>
        get() = (rules.schemaRules as RadarSchemaRules).schemaStore

    companion object {
        private const val AVRO_EXTENSION = "avsc"

        /** Formats a stream of validation exceptions.  */
        fun format(exceptions: List<ValidationException>): String {
            return exceptions.joinToString(separator = "") { ex: ValidationException ->
                """
                     |Validation FAILED:
                     |${ex.message}
                     |
                     |
                     |
                """.trimMargin()
            }
        }

        fun isAvscFile(file: Path): Boolean = file.extension.equals(AVRO_EXTENSION, ignoreCase = true)
    }
}
