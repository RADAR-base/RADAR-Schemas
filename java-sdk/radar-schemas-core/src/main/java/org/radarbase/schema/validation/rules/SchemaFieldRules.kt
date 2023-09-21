package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Type.ENUM
import org.apache.avro.Schema.Type.RECORD
import org.apache.avro.Schema.Type.UNION

interface SchemaFieldRules {
    /** Recursively checks field types.  */
    fun validateFieldTypes(schemaRules: SchemaRules): Validator<SchemaField>

    /** Checks field name format.  */
    fun validateFieldName(): Validator<SchemaField>

    /** Checks field documentation presence and format.  */
    fun validateFieldDocumentation(): Validator<SchemaField>

    /** Checks field default values.  */
    fun validateDefault(): Validator<SchemaField>

    /** Get a validator for a field.  */
    fun getValidator(schemaRules: SchemaRules): Validator<SchemaField> {
        return validateFieldTypes(schemaRules)
            .and(validateFieldName())
            .and(validateDefault())
            .and(validateFieldDocumentation())
    }

    /** Get a validator for a union inside a record.  */
    fun validateInternalUnion(schemaRules: SchemaRules): Validator<SchemaField> {
        return Validator { field: SchemaField ->
            field.field.schema().types.stream()
                .flatMap { schema: Schema ->
                    val type = schema.type
                    return@flatMap when (type) {
                        RECORD -> schemaRules.validateRecord().validate(schema)
                        ENUM -> schemaRules.validateEnum().validate(schema)
                        UNION -> Validator.raise(
                            message(field, "Cannot have a nested union."),
                        )
                        else -> Validator.valid()
                    }
                }
        }
    }

    companion object {
        /** A message function for a field, ending with given text.  */
        fun message(field: SchemaField, text: String): String {
            return "Field ${field.field.name()} in schema ${field.schema.fullName} is invalid. $text"
        }
    }
}
