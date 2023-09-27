package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationHelper.getNamespace
import org.radarbase.schema.validation.ValidationHelper.toRecordName
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

    override val isSchemaLocationCorrect = all(
        isNamespaceSchemaLocationCorrect(),
        isNameSchemaLocationCorrect(),
    )

    private fun isNamespaceSchemaLocationCorrect() = Validator<SchemaMetadata> { metadata ->
        try {
            val expected = getNamespace(schemaRoot, metadata.path, metadata.scope)
            val namespace = metadata.schema.namespace
            if (!expected.equals(namespace, ignoreCase = true)) {
                raise(
                    metadata,
                    "Namespace cannot be null and must fully lowercase dot separated without numeric. In this case the expected value is \"$expected\".",
                )
            }
        } catch (ex: IllegalArgumentException) {
            raise("Path ${metadata.path} is not part of root $schemaRoot", ex)
        }
    }

    private fun isNameSchemaLocationCorrect() = Validator<SchemaMetadata> { metadata ->
        val expected = metadata.path.toRecordName()
        if (!expected.equals(metadata.schema.name, ignoreCase = true)) {
            raise(metadata, "Record name should match file name. Expected record name is \"$expected\".")
        }
    }

    override fun isSchemaCorrect(validator: Validator<Schema>) = Validator<SchemaMetadata> { metadata ->
        if (pathMatcher.matches(metadata.path)) {
            validator.launchValidation(metadata.schema)
        }
    }
}
