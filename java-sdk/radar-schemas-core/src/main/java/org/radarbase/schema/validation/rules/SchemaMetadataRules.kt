package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.Scope
import org.radarbase.schema.validation.ValidationContext

interface SchemaMetadataRules {
    val schemaRules: SchemaRules

    /** Checks the location of a schema with its internal data.  */
    val isSchemaLocationCorrect: Validator<SchemaMetadata>

    /**
     * Validates any schema file. It will choose the correct validation method based on the scope
     * and type of the schema.
     */
    fun isSchemaMetadataValid(scopeSpecificValidation: Boolean) = Validator<SchemaMetadata> { metadata ->
        isSchemaLocationCorrect.launchValidation(metadata)

        val ruleset = when {
            metadata.schema.type == Schema.Type.ENUM -> schemaRules.isEnumValid
            !scopeSpecificValidation -> schemaRules.isRecordValid
            metadata.scope == Scope.ACTIVE -> schemaRules.isActiveSourceValid
            metadata.scope == Scope.MONITOR -> schemaRules.isMonitorSourceValid
            metadata.scope == Scope.PASSIVE -> schemaRules.isPassiveSourceValid
            else -> schemaRules.isRecordValid
        }
        isSchemaCorrect(ruleset).launchValidation(metadata)
    }

    /** Validates schemas without their metadata.  */
    fun isSchemaCorrect(validator: Validator<Schema>) = Validator<SchemaMetadata> { metadata ->
        validator.launchValidation(metadata.schema)
    }
}

fun ValidationContext.raise(metadata: SchemaMetadata, text: String) {
    raise("Schema ${metadata.schema.fullName} at ${metadata.path} is invalid. $text")
}
