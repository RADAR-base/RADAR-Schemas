package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.Scope

interface SchemaMetadataRules {
    val schemaRules: SchemaRules

    /** Checks the location of a schema with its internal data.  */
    fun validateSchemaLocation(): Validator<SchemaMetadata>

    /**
     * Validates any schema file. It will choose the correct validation method based on the scope
     * and type of the schema.
     */
    fun getValidator(validateScopeSpecific: Boolean): Validator<SchemaMetadata> =
        Validator { metadata: SchemaMetadata ->
            val schemaRules = schemaRules

            var validator = validateSchemaLocation()
            validator = if (metadata.schema.type == Schema.Type.ENUM) {
                validator.and(schema(schemaRules.validateEnum()))
            } else if (validateScopeSpecific) {
                when (metadata.scope) {
                    Scope.ACTIVE -> validator.and(schema(schemaRules.validateActiveSource()))
                    Scope.MONITOR -> validator.and(schema(schemaRules.validateMonitor()))
                    Scope.PASSIVE -> validator.and(schema(schemaRules.validatePassive()))
                    else -> validator.and(schema(schemaRules.validateRecord()))
                }
            } else {
                validator.and(schema(schemaRules.validateRecord()))
            }
            validator.apply(metadata)
        }

    /** Validates schemas without their metadata.  */
    fun schema(validator: Validator<Schema>): Validator<SchemaMetadata> =
        Validator { metadata: SchemaMetadata -> validator.apply(metadata.schema) }

    fun message(text: String): (SchemaMetadata) -> String = { metadata ->
        "Schema ${metadata.schema.fullName} at ${metadata.path} is invalid. $text"
    }
}
