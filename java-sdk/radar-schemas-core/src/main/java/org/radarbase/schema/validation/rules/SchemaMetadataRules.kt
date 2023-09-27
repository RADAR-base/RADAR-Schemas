package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.Scope
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationContext
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
class SchemaMetadataRules(
    private val schemaRoot: Path,
    config: SchemaConfig,
    val schemaRules: SchemaRules = SchemaRules(),
) {
    private val pathMatcher: PathMatcher = config.pathMatcher(schemaRoot)

    val isSchemaLocationCorrect = all(
        isNamespaceSchemaLocationCorrect(),
        isNameSchemaLocationCorrect(),
    )

    /**
     * Validates any schema file. It will choose the correct validation method based on the scope
     * and type of the schema.
     */
    fun isSchemaMetadataValid(scopeSpecificValidation: Boolean) = Validator<SchemaMetadata> { metadata ->
        if (!pathMatcher.matches(metadata.path)) {
            return@Validator
        }

        isSchemaLocationCorrect.launchValidation(metadata)

        val ruleset = when {
            metadata.schema.type == Schema.Type.ENUM -> schemaRules.isEnumValid
            !scopeSpecificValidation -> schemaRules.isRecordValid
            metadata.scope == Scope.ACTIVE -> schemaRules.isActiveSourceValid
            metadata.scope == Scope.MONITOR -> schemaRules.isMonitorSourceValid
            metadata.scope == Scope.PASSIVE -> schemaRules.isPassiveSourceValid
            else -> schemaRules.isRecordValid
        }
        ruleset.launchValidation(metadata.schema)
    }

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
}

fun ValidationContext.raise(metadata: SchemaMetadata, text: String) {
    raise("Schema ${metadata.schema.fullName} at ${metadata.path} is invalid. $text")
}
