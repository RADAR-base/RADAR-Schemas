package org.radarbase.schema.validation.rules

import org.apache.avro.JsonProperties
import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.Schema.Type.ENUM
import org.apache.avro.Schema.Type.NULL
import org.apache.avro.Schema.Type.RECORD
import org.apache.avro.Schema.Type.UNION
import org.radarbase.schema.validation.ValidationContext
import org.radarbase.schema.validation.rules.SchemaRules.Companion.validateDocumentation
import java.util.EnumMap

/**
 * Rules for RADAR-Schemas schema fields.
 */
class SchemaFieldRules {
    private val defaultsValidator: MutableMap<Type, Validator<SchemaField>>
    internal lateinit var schemaRules: SchemaRules

    /**
     * Rules for RADAR-Schemas schema fields.
     */
    init {
        defaultsValidator = EnumMap(Type::class.java)
        defaultsValidator[ENUM] = Validator { isEnumDefaultUnknown(it) }
        defaultsValidator[UNION] = Validator { isDefaultUnionCompatible(it) }
    }

    /** Get a validator for a union inside a record.  */
    private val validateInternalUnion = Validator<SchemaField> { field ->
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

    val isFieldTypeValid: Validator<SchemaField> = Validator { field ->
        val schema = field.field.schema()
        val subType = schema.type
        when (subType) {
            UNION -> validateInternalUnion.validate(field)
            RECORD -> schemaRules.isRecordValid.validate(schema)
            ENUM -> schemaRules.isEnumValid.validate(schema)
            else -> Unit
        }
    }

    private val isDefaultValueNullable = Validator<SchemaField> { field ->
        if (field.field.defaultVal() != null) {
            raise(
                field,
                "Default of type ${field.field.schema().type} is set to ${field.field.defaultVal()}. " +
                    "The only acceptable default values are the \"UNKNOWN\" enum symbol and null.",
            )
        }
    }

    val isDefaultValueValid = Validator { input: SchemaField ->
        defaultsValidator
            .getOrDefault(
                input.field.schema().type,
                isDefaultValueNullable,
            )
            .validate(input)
    }

    val isNameValid = validator<SchemaField>(
        predicate = { f -> f.field.name()?.matches(FIELD_NAME_PATTERN) == true },
        message = "Field name does not respect lowerCamelCase name convention. " +
            "Please avoid abbreviations and write out the field name instead.",
    )

    val isDocumentationValid = Validator { field: SchemaField ->
        validateDocumentation(
            doc = field.field.doc(),
            raise = ValidationContext::raise,
            schema = field,
        )
    }

    val isFieldValid: Validator<SchemaField> = all(
        isFieldTypeValid,
        isNameValid,
        isDefaultValueValid,
        isDocumentationValid,
    )

    private fun ValidationContext.isEnumDefaultUnknown(field: SchemaField) {
        if (
            field.field.schema().enumSymbols.contains(UNKNOWN) &&
            field.field.defaultVal()?.toString() != UNKNOWN
        ) {
            raise(
                field,
                "Default is \"${field.field.defaultVal()}\". Any Avro enum type that has an \"UNKNOWN\" symbol must set its default value to \"UNKNOWN\".",
            )
        }
    }

    private fun ValidationContext.isDefaultUnionCompatible(field: SchemaField) {
        if (
            field.field.schema().types.contains(NULL_SCHEMA) &&
            field.field.defaultVal() != JsonProperties.NULL_VALUE
        ) {
            raise(
                field,
                "Default is not null. Any nullable Avro field must specify have its default value set to null.",
            )
        }
    }

    companion object {
        private const val UNKNOWN = "UNKNOWN"
        private val NULL_SCHEMA = Schema.create(NULL)

        // lowerCamelCase
        internal val FIELD_NAME_PATTERN = "[a-z][a-z0-9]*([a-z0-9][A-Z][a-z0-9]+)?([A-Z][a-z0-9]+)*[A-Z]?".toRegex()
    }
}

fun ValidationContext.raise(field: SchemaField, text: String) {
    raise("Field ${field.field.name()} in schema ${field.schema.fullName} is invalid. $text")
}
