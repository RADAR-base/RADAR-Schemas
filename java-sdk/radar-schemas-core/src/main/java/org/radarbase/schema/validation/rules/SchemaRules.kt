package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Type.RECORD
import org.radarbase.schema.validation.ValidationContext

interface SchemaRules {
    val fieldRules: SchemaFieldRules

    /**
     * Checks that schemas are unique compared to already validated schemas.
     */
    val isUnique: Validator<Schema>

    /**
     * Checks schema namespace format.
     */
    val isNamespaceValid: Validator<Schema>

    /**
     * Checks schema name format.
     */
    val isNameValid: Validator<Schema>

    /**
     * Checks schema documentation presence and format.
     */
    val isDocumentationValid: Validator<Schema>

    /**
     * Checks that the symbols of enums have the required format.
     */
    val isEnumSymbolsValid: Validator<Schema>

    /**
     * Checks that schemas should have a `time` field.
     */
    val hasTime: Validator<Schema>

    /**
     * Checks that schemas should have a `timeCompleted` field.
     */
    val hasTimeCompleted: Validator<Schema>

    /**
     * Checks that schemas should not have a `timeCompleted` field.
     */
    val hasNoTimeCompleted: Validator<Schema>

    /**
     * Checks that schemas should have a `timeReceived` field.
     */
    val hasTimeReceived: Validator<Schema>

    /**
     * Checks that schemas should not have a `timeReceived` field.
     */
    val hasNoTimeReceived: Validator<Schema>

    /**
     * Validate an enum.
     */
    val isEnumValid: Validator<Schema>

    /**
     * Validate a record that is defined inline.
     */
    val isRecordValid: Validator<Schema>
    val isAvroConnectCompatible: Validator<Schema>

    /**
     * Validates record schemas of an active source.
     */
    val isActiveSourceValid: Validator<Schema>

    /**
     * Validates schemas of monitor sources.
     */
    val isMonitorSourceValid: Validator<Schema>

    /**
     * Validates schemas of passive sources.
     */
    val isPassiveSourceValid: Validator<Schema>

    fun schemaErrorMessage(text: String): (Schema) -> String {
        return { schema -> "Schema ${schema.fullName} is invalid. $text" }
    }

    /**
     * Validates all fields of records.
     * Validation will fail on non-record types or records with no fields.
     */
    fun isFieldsValid(validator: Validator<SchemaField>) = Validator { schema: Schema ->
        if (schema.type != RECORD) {
            raise("Default validation can be applied only to an Avro RECORD, not to ${schema.type} of schema ${schema.fullName}.")
            return@Validator
        }
        if (schema.fields.isEmpty()) {
            raise("Schema ${schema.fullName} does not contain any fields.")
            return@Validator
        }
        schema.fields.forEach { field ->
            validator.launchValidation(SchemaField(schema, field))
        }
    }
}

fun ValidationContext.raise(schema: Schema, text: String) {
    raise("Schema ${schema.fullName} is invalid. $text")
}
