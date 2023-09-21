package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Type.RECORD
import org.radarbase.schema.validation.rules.Validator.Companion.raise

interface SchemaRules {
    val fieldRules: SchemaFieldRules

    /**
     * Checks that schemas are unique compared to already validated schemas.
     */
    fun validateUniqueness(): Validator<Schema>

    /**
     * Checks schema namespace format.
     */
    fun validateNameSpace(): Validator<Schema>

    /**
     * Checks schema name format.
     */
    fun validateName(): Validator<Schema>

    /**
     * Checks schema documentation presence and format.
     */
    fun validateSchemaDocumentation(): Validator<Schema>

    /**
     * Checks that the symbols of enums have the required format.
     */
    fun validateSymbols(): Validator<Schema>

    /**
     * Checks that schemas should have a `time` field.
     */
    fun validateTime(): Validator<Schema>

    /**
     * Checks that schemas should have a `timeCompleted` field.
     */
    fun validateTimeCompleted(): Validator<Schema>

    /**
     * Checks that schemas should not have a `timeCompleted` field.
     */
    fun validateNotTimeCompleted(): Validator<Schema>

    /**
     * Checks that schemas should have a `timeReceived` field.
     */
    fun validateTimeReceived(): Validator<Schema>

    /**
     * Checks that schemas should not have a `timeReceived` field.
     */
    fun validateNotTimeReceived(): Validator<Schema>

    /**
     * Validate an enum.
     */
    fun validateEnum(): Validator<Schema> = validateUniqueness()
        .and(validateNameSpace())
        .and(validateSymbols())
        .and(validateSchemaDocumentation())
        .and(validateName())

    /**
     * Validate a record that is defined inline.
     */
    fun validateRecord(): Validator<Schema> = validateUniqueness()
        .and(validateAvroData())
        .and(validateNameSpace())
        .and(validateName())
        .and(validateSchemaDocumentation())
        .and(fields(fieldRules.getValidator(this)))

    fun validateAvroData(): Validator<Schema>

    /**
     * Validates record schemas of an active source.
     */
    fun validateActiveSource(): Validator<Schema> = validateRecord()
        .and(
            validateTime()
                .and(validateTimeCompleted())
                .and(validateNotTimeReceived()),
        )

    /**
     * Validates schemas of monitor sources.
     */
    fun validateMonitor(): Validator<Schema> = validateRecord()
        .and(validateTime())

    /**
     * Validates schemas of passive sources.
     */
    fun validatePassive(): Validator<Schema> = validateRecord()
        .and(validateTime())
        .and(validateTimeReceived())
        .and(validateNotTimeCompleted())

    fun messageSchema(text: String): (Schema) -> String {
        return { schema -> "Schema ${schema.fullName} is invalid. $text" }
    }

    fun messageSchema(schema: Schema, text: String): String {
        return "Schema ${schema.fullName} is invalid. $text"
    }

    /**
     * Validates all fields of records.
     * Validation will fail on non-record types or records with no fields.
     */
    fun fields(validator: Validator<SchemaField>) = Validator { schema: Schema ->
        if (schema.type != RECORD) {
            return@Validator raise(
                "Default validation can be applied only to an Avro RECORD, not to " +
                    schema.type + " of schema " + schema.fullName + '.',
            )
        }
        if (schema.fields.isEmpty()) {
            return@Validator raise("Schema " + schema.fullName + " does not contain any fields.")
        }
        schema.fields.stream()
            .flatMap { field ->
                validator.validate(
                    SchemaField(
                        schema,
                        field,
                    ),
                )
            }
    }
}
