package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationHelper
import java.nio.file.Path
import java.nio.file.PathMatcher

/** Rules for schemas with metadata in RADAR-Schemas.  */
class RadarSchemaMetadataRules
/**
 * Rules for schemas with metadata in RADAR-Schemas.
 * @param schemaRoot directory of RADAR-Schemas commons
 * @param config configuration for excluding schemas from validation.
 * @param schemaRules schema rules implementation.
 */
@JvmOverloads constructor(
    private val schemaRoot: Path,
    config: SchemaConfig,
    override val schemaRules: SchemaRules = RadarSchemaRules(),
) : SchemaMetadataRules {
    private val pathMatcher: PathMatcher = config.pathMatcher(schemaRoot)

    override fun validateSchemaLocation(): Validator<SchemaMetadata> =
        validateNamespaceSchemaLocation()
            .and(validateNameSchemaLocation())

    private fun validateNamespaceSchemaLocation(): Validator<SchemaMetadata> =
        Validator { metadata: SchemaMetadata ->
            try {
                val expected = ValidationHelper.getNamespace(
                    schemaRoot,
                    metadata.path,
                    metadata.scope,
                )
                val namespace = metadata.schema.namespace
                return@Validator Validator.check(
                    expected.equals(namespace, ignoreCase = true),
                    message(
                        "Namespace cannot be null and must fully lowercase dot separated without numeric. In this case the expected value is \"$expected\".",
                    ).invoke(metadata),
                )
            } catch (ex: IllegalArgumentException) {
                return@Validator Validator.raise(
                    "Path " + metadata.path +
                        " is not part of root " + schemaRoot,
                    ex,
                )
            }
        }

    private fun validateNameSchemaLocation(): Validator<SchemaMetadata> =
        Validator { metadata: SchemaMetadata ->
            val expected = ValidationHelper.getRecordName(metadata.path)
            if (expected.equals(
                    metadata.schema.name,
                    ignoreCase = true,
                )
            ) {
                Validator.valid()
            } else {
                Validator.raise(
                    message(
                        "Record name should match file name. Expected record name is \"$expected\".",
                    ).invoke(metadata),
                )
            }
        }

    override fun schema(validator: Validator<Schema>): Validator<SchemaMetadata> =
        Validator { metadata: SchemaMetadata ->
            if (pathMatcher.matches(metadata.path)) {
                validator.apply(
                    metadata.schema,
                )
            } else {
                Validator.valid()
            }
        }
}
