package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Type.ENUM
import org.apache.avro.Schema.Type.RECORD
import org.apache.avro.Schema.Type.UNION
import org.radarbase.schema.validation.ValidationContext

interface SchemaFieldRules {
    /** Recursively checks field types.  */
    fun validateFieldTypes(schemaRules: SchemaRules): Validator<SchemaField>

    /** Checks field name format.  */
    val isNameValid: Validator<SchemaField>

    /** Checks field documentation presence and format.  */
    val isDocumentationValid: Validator<SchemaField>

    /** Checks field default values.  */
    val isDefaultValueValid: Validator<SchemaField>

    /** Get a validator for a field.  */
    fun isFieldValid(schemaRules: SchemaRules): Validator<SchemaField> = all(
        validateFieldTypes(schemaRules),
        isNameValid,
        isDefaultValueValid,
        isDocumentationValid,
    )

    /** Get a validator for a union inside a record.  */
    fun validateInternalUnion(schemaRules: SchemaRules) = Validator { field: SchemaField ->
        field.field.schema().types
            .forEach { schema: Schema ->
                val type = schema.type
                when (type) {
                    RECORD -> schemaRules.isRecordValid.launchValidation(schema)
                    ENUM -> schemaRules.isEnumValid.launchValidation(schema)
                    UNION -> raise(field, "Cannot have a nested union.")
                    else -> Unit
                }
            }
    }
}

fun ValidationContext.raise(field: SchemaField, text: String) {
    raise("Field ${field.field.name()} in schema ${field.schema.fullName} is invalid. $text")
}
