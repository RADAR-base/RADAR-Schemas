package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

import static org.radarcns.schema.validation.rules.Validator.raise;

public interface SchemaValidationRules {
    /** Checks the location of a schema with its internal data. */
    Validator<SchemaMetadata> validateSchemaLocation();

    /** Checks schema namespace format. */
    Validator<Schema> validateNameSpace();

    /** Checks schema name format. */
    Validator<Schema> validateName();

    /** Checks schema documentation presence and format. */
    Validator<Schema> validateSchemaDocumentation();

    /** Recursively checks field types. */
    Validator<SchemaField> validateFieldTypes();

    /** Validate an enum that is defined inline. */
    default Validator<SchemaMetadata> internalEnumValidation() {
        return schema(validateSymbols())
                .and(schema(validateSchemaDocumentation()))
                .and(schema(validateName()));
    }

    /** Validate a record that is defined inline. */
    default Validator<SchemaMetadata> internalRecordValidation() {
        return schema(validateNameSpace())
                .and(schema(validateName()))
                .and(schema(validateSchemaDocumentation()))
                .and(fields(validateFieldTypes()))
                .and(fields(validateFieldName()))
                .and(fields(validateDefault()))
                .and(fields(validateFieldDocumentation()));
    }

    /** Checks field name format. */
    Validator<SchemaField> validateFieldName();

    /** Checks field documentation presence and format. */
    Validator<SchemaField> validateFieldDocumentation();

    /** Checks that the symbols of enums have the required format. */
    Validator<Schema> validateSymbols();

    Validator<SchemaField> validateDefault();

    /** Checks that schemas should have a {@code time} field. */
    Validator<Schema> validateTime();

    /** Checks that schemas should have a {@code timeCompleted} field. */
    Validator<Schema> validateTimeCompleted();

    /** Checks that schemas should not have a {@code timeCompleted} field. */
    Validator<Schema> validateNotTimeCompleted();

    /** Checks that schemas should have a {@code timeReceived} field. */
    Validator<Schema> validateTimeReceived();

    /** Checks that schemas should not have a {@code timeReceived} field. */
    Validator<Schema> validateNotTimeReceived();

    /**
     * Validate record schemas.
     * @return TODO
     */
    default Validator<SchemaMetadata> validateGeneralRecord() {
        return validateSchemaLocation()
                .and(schema(validateNameSpace()))
                .and(schema(validateName()))
                .and(schema(validateSchemaDocumentation()))
                .and(fields(validateFieldTypes()))
                .and(fields(validateFieldName()))
                .and(fields(validateDefault()))
                .and(fields(validateFieldDocumentation()));
    }

    /**
     * Validates record schemas of an active source
     * @return TODO
     */
    default Validator<SchemaMetadata> validateActiveSource() {
        return validateGeneralRecord()
                .and(schema(validateTime()))
                .and(schema(validateTimeCompleted()))
                .and(schema(validateNotTimeReceived()));
    }

    /**
     * Validates schemas of monitor sources.
     * @return TODO
     */
    default Validator<SchemaMetadata> validateMonitor() {
        return validateGeneralRecord()
                .and(schema(validateTime()));
    }

    /**
     * Validates schemas of passive sources.
     */
    default Validator<SchemaMetadata> validatePassive() {
        return validateGeneralRecord()
                .and(schema(validateTime()))
                .and(schema(validateTimeReceived()))
                .and(schema(validateNotTimeCompleted()));
    }

    /**
     * Validates enum schemas in standalone files.
     */
    default Validator<SchemaMetadata> validateEnum() {
        return validateSchemaLocation()
                .and(schema(validateNameSpace()))
                .and(schema(validateName()))
                .and(schema(validateSchemaDocumentation()))
                .and(schema(validateSymbols()));
    }

    /**
     * Validates any schema file. It will choose the correct validation method based on the scope
     * and type of the schema.
     */
    default Validator<SchemaMetadata> getValidator() {
        return schema -> {
            if (schema.getSchema().getType().equals(Schema.Type.ENUM)) {
                return validateEnum().apply(schema);
            } else {
                switch (schema.getScope()) {
                    case ACTIVE:
                        return validateActiveSource().apply(schema);
                    case CATALOGUE:
                        return validateGeneralRecord().apply(schema);
                    case KAFKA:
                        return validateGeneralRecord().apply(schema);
                    case MONITOR:
                        return validateMonitor().apply(schema);
                    case PASSIVE:
                        return validatePassive().apply(schema);
                    default:
                        return validateGeneralRecord().apply(schema);
                }
            }
        };
    }

    /**
     * Validates all fields of records.
     * Validation will fail on non-record types or records with no fields.
     */
    default Validator<SchemaMetadata> fields(Validator<SchemaField> validator) {
        return metadata -> {
            Schema schema = metadata.getSchema();
            if (!schema.getType().equals(Schema.Type.RECORD)) {
                return raise("Default validation can be applied only to an Avro RECORD, not to "
                        + schema.getType() + " of schema " + schema.getFullName() + '.');
            }
            if (schema.getFields().isEmpty()) {
                return raise("Schema " + schema.getFullName() + " does not contain any fields.");
            }
            return schema.getFields().stream()
                    .flatMap(field -> validator.apply(new SchemaField(metadata, field)));
        };
    }

    /** Validates schemas without their metadata. */
    default Validator<SchemaMetadata> schema(Validator<Schema> validator) {
        return metadata -> validator.apply(metadata.getSchema());
    }
}
