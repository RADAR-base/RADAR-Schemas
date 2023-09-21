package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationHelper
import org.radarbase.schema.validation.rules.Validator.Companion.check
import org.radarbase.schema.validation.rules.Validator.Companion.raise
import org.radarbase.schema.validation.rules.Validator.Companion.valid
import java.nio.file.Path
import java.nio.file.PathMatcher

/** Rules for schemas with metadata in RADAR-Schemas.
 *
 * @param schemaRoot directory of RADAR-Schemas commons
 * @param config configuration for excluding schemas from validation.
 * @param schemaRules schema rules implementation.
 */
class RadarSchemaMetadataRules(
    private val schemaRoot: Path,
    config: SchemaConfig,
    override val schemaRules: SchemaRules = RadarSchemaRules(),
) : SchemaMetadataRules {
    private val pathMatcher: PathMatcher = config.pathMatcher(schemaRoot)

    override fun validateSchemaLocation(): Validator<SchemaMetadata> =
        validateNamespaceSchemaLocation()
            .and(validateNameSchemaLocation())

    private fun validateNamespaceSchemaLocation(): Validator<SchemaMetadata> =
        Validator { metadata ->
            try {
                val expected = ValidationHelper.getNamespace(
                    schemaRoot,
                    metadata.path,
                    metadata.scope,
                )
                val namespace = metadata.schema?.namespace
                return@Validator check(
                    expected.equals(namespace, ignoreCase = true),
                    message(
                        metadata,
                        "Namespace cannot be null and must fully lowercase dot separated without numeric. In this case the expected value is \"$expected\".",
                    ),
                )
            } catch (ex: IllegalArgumentException) {
                return@Validator raise(
                    "Path " + metadata.path +
                        " is not part of root " + schemaRoot,
                    ex,
                )
            }
        }

    private fun validateNameSchemaLocation(): Validator<SchemaMetadata> =
        Validator { metadata ->
            if (metadata.path == null) {
                return@Validator raise(message(metadata, "Missing metadata path"))
            }
            val expected = ValidationHelper.getRecordName(metadata.path)
            if (expected.equals(metadata.schema?.name, ignoreCase = true)) {
                valid()
            } else {
                raise(message(metadata, "Record name should match file name. Expected record name is \"$expected\"."))
            }
        }

    override fun schema(validator: Validator<Schema>): Validator<SchemaMetadata> =
        Validator { metadata ->
            when {
                metadata.schema == null -> raise("Missing schema")
                pathMatcher.matches(metadata.path) -> validator.validate(metadata.schema)
                else -> valid()
            }
        }
}
