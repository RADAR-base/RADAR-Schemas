package org.radarbase.schema.validation.rules

import org.apache.avro.JsonProperties
import org.apache.avro.Schema
import org.apache.avro.Schema.Type
import org.apache.avro.Schema.Type.ENUM
import org.apache.avro.Schema.Type.NULL
import org.apache.avro.Schema.Type.RECORD
import org.apache.avro.Schema.Type.UNION
import org.radarbase.schema.validation.ValidationException
import org.radarbase.schema.validation.rules.RadarSchemaRules.Companion.validateDocumentation
import org.radarbase.schema.validation.rules.SchemaFieldRules.Companion.message
import org.radarbase.schema.validation.rules.Validator.Companion.check
import org.radarbase.schema.validation.rules.Validator.Companion.validate
import java.util.EnumMap
import java.util.stream.Stream

/**
 * Rules for RADAR-Schemas schema fields.
 */
class RadarSchemaFieldRules : SchemaFieldRules {
    private val defaultsValidator: MutableMap<Type, Validator<SchemaField>>

    /**
     * Rules for RADAR-Schemas schema fields.
     */
    init {
        defaultsValidator = EnumMap(Type::class.java)
        defaultsValidator[ENUM] = Validator { validateDefaultEnum(it) }
        defaultsValidator[UNION] = Validator { validateDefaultUnion(it) }
    }

    override fun validateFieldTypes(schemaRules: SchemaRules): Validator<SchemaField> {
        return Validator { field ->
            val schema = field.field.schema()
            val subType = schema.type
            return@Validator when (subType) {
                UNION -> validateInternalUnion(schemaRules).validate(field)
                RECORD -> schemaRules.validateRecord().validate(schema)
                ENUM -> schemaRules.validateEnum().validate(schema)
                else -> Validator.valid()
            }
        }
    }

    override fun validateDefault(): Validator<SchemaField> {
        return Validator { input: SchemaField ->
            defaultsValidator
                .getOrDefault(
                    input.field.schema().type,
                    Validator { validateDefaultOther(it) },
                )
                .validate(input)
        }
    }

    override fun validateFieldName(): Validator<SchemaField> {
        return validate(
            { f -> f.field.name()?.matches(FIELD_NAME_PATTERN) == true },
            "Field name does not respect lowerCamelCase name convention." +
                " Please avoid abbreviations and write out the field name instead.",
        )
    }

    override fun validateFieldDocumentation(): Validator<SchemaField> {
        return Validator { field: SchemaField ->
            validateDocumentation(
                field.field.doc(),
                { m, f -> message(f, m) },
                field,
            )
        }
    }

    private fun validateDefaultEnum(field: SchemaField): Stream<ValidationException> {
        return check(
            !field.field.schema().enumSymbols.contains(UNKNOWN) ||
                field.field.defaultVal() != null && field.field.defaultVal()
                    .toString() == UNKNOWN,
            message(
                field,
                "Default is \"" + field.field.defaultVal() +
                    "\". Any Avro enum type that has an \"UNKNOWN\" symbol must set its" +
                    " default value to \"UNKNOWN\".",
            ),
        )
    }

    private fun validateDefaultUnion(field: SchemaField): Stream<ValidationException> {
        return check(
            !field.field.schema().types.contains(Schema.create(NULL)) ||
                field.field.defaultVal() != null && field.field.defaultVal() == JsonProperties.NULL_VALUE,
            message(
                field,
                "Default is not null. Any nullable Avro field must" +
                    " specify have its default value set to null.",
            ),
        )
    }

    private fun validateDefaultOther(field: SchemaField): Stream<ValidationException> {
        return check(
            field.field.defaultVal() == null,
            message(
                field,
                "Default of type " + field.field.schema().type + " is set to " +
                    field.field.defaultVal() + ". The only acceptable default values are the" +
                    " \"UNKNOWN\" enum symbol and null.",
            ),
        )
    }

    companion object {
        private const val UNKNOWN = "UNKNOWN"

        // lowerCamelCase
        internal val FIELD_NAME_PATTERN = "[a-z][a-z0-9]*([a-z0-9][A-Z][a-z0-9]+)?([A-Z][a-z0-9]+)*[A-Z]?".toRegex()
    }
}
