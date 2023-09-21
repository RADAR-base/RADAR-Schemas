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
import org.radarbase.schema.validation.ValidationHelper.matchesExtension
import org.radarbase.schema.validation.rules.RadarSchemaMetadataRules
import org.radarbase.schema.validation.rules.RadarSchemaRules
import org.radarbase.schema.validation.rules.SchemaMetadata
import org.radarbase.schema.validation.rules.SchemaMetadataRules
import org.radarbase.schema.validation.rules.Validator
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.Arrays
import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Validator for a set of RADAR-Schemas.
 *
 * @param schemaRoot RADAR-Schemas commons directory.
 * @param config configuration to exclude certain schemas or fields from validation.
 */
class SchemaValidator(schemaRoot: Path, config: SchemaConfig) {
    val rules: SchemaMetadataRules = RadarSchemaMetadataRules(schemaRoot, config)
    private val pathMatcher: PathMatcher = config.pathMatcher(schemaRoot)
    private var validator: Validator<SchemaMetadata> = rules.getValidator(false)

    fun analyseSourceCatalogue(
        scope: Scope?,
        catalogue: SourceCatalogue,
    ): Stream<ValidationException> {
        validator = rules.getValidator(true)
        val producers: Stream<DataProducer<*>> = if (scope != null) {
            catalogue.sources.stream()
                .filter { it.scope == scope }
        } else {
            catalogue.sources.stream()
        }
        return try {
            producers
                .flatMap { it.data.stream() }
                .flatMap { topic ->
                    val (keySchema, valueSchema) = catalogue.schemaCatalogue.getSchemaMetadata(topic)
                    Stream.of(keySchema, valueSchema).filter { it.schema != null }
                }
                .sorted(Comparator.comparing { it.schema!!.fullName })
                .distinct()
                .flatMap(this::validate)
                .distinct()
        } finally {
            validator = rules.getValidator(false)
        }
    }

    fun analyseFiles(
        scope: Scope?,
        schemaCatalogue: SchemaCatalogue,
    ): Stream<ValidationException> {
        if (scope == null) {
            return analyseFiles(schemaCatalogue)
        }
        validator = rules.getValidator(false)
        val useTypes = buildMap {
            schemaCatalogue.schemas.forEach { (key, value) ->
                if (value.scope == scope) {
                    put(key, value.schema)
                }
            }
        }

        return Stream.concat(
            schemaCatalogue.unmappedAvroFiles.stream()
                .filter { s -> s.scope == scope && s.path != null }
                .map { p ->
                    val parser = Schema.Parser()
                    parser.addTypes(useTypes)
                    try {
                        parser.parse(p.path?.toFile())
                        return@map null
                    } catch (ex: Exception) {
                        return@map ValidationException("Cannot parse schema", ex)
                    }
                }
                .filter(Objects::nonNull)
                .map { obj -> requireNotNull(obj) },
            schemaCatalogue.schemas.values.stream()
                .flatMap { this.validate(it) },
        ).distinct()
    }
    private fun analyseFiles(schemaCatalogue: SchemaCatalogue): Stream<ValidationException> =
        Arrays.stream(Scope.entries.toTypedArray())
            .flatMap { scope -> analyseFiles(scope, schemaCatalogue) }

    /** Validate a single schema in given path.  */
    fun validate(schema: Schema, path: Path, scope: Scope): Stream<ValidationException> =
        validate(SchemaMetadata(schema, scope, path))

    /** Validate a single schema in given path.  */
    private fun validate(schemaMetadata: SchemaMetadata): Stream<ValidationException> =
        if (pathMatcher.matches(schemaMetadata.path)) {
            validator.validate(schemaMetadata)
        } else {
            Stream.empty()
        }

    val validatedSchemas: Map<String, Schema>
        get() = (rules.schemaRules as RadarSchemaRules).schemaStore

    companion object {
        private const val AVRO_EXTENSION = "avsc"

        /** Formats a stream of validation exceptions.  */
        @JvmStatic
        fun format(exceptionStream: Stream<ValidationException>): String {
            return exceptionStream
                .map { ex: ValidationException ->
                    """
                     |Validation FAILED:
                     |${ex.message}
                     |
                     |
                     |
                    """.trimMargin()
                }
                .collect(Collectors.joining())
        }

        fun isAvscFile(file: Path): Boolean =
            matchesExtension(file, AVRO_EXTENSION)
    }
}
