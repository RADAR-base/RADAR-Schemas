package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

import java.util.function.Function;

import static org.radarcns.schema.validation.rules.Validator.raise;

public interface SchemaRules {
    SchemaFieldRules getFieldRules();

    /** Checks that schemas are unique compared to already validated schemas. */
    Validator<Schema> validateUniqueness();

    /** Checks schema namespace format. */
    Validator<Schema> validateNameSpace();

    /** Checks schema name format. */
    Validator<Schema> validateName();

    /** Checks schema documentation presence and format. */
    Validator<Schema> validateSchemaDocumentation();

    /** Checks that the symbols of enums have the required format. */
    Validator<Schema> validateSymbols();

    /**
     * Checks that schemas should have a {@code time} field.
     */
    Validator<Schema> validateTime();

    /**
     * Checks that schemas should have a {@code timeCompleted} field.
     */
    Validator<Schema> validateTimeCompleted();

    /**
     * Checks that schemas should not have a {@code timeCompleted} field.
     */
    Validator<Schema> validateNotTimeCompleted();

    /**
     * Checks that schemas should have a {@code timeReceived} field.
     */
    Validator<Schema> validateTimeReceived();

    /**
     * Checks that schemas should not have a {@code timeReceived} field.
     */
    Validator<Schema> validateNotTimeReceived();

    /** Validate an enum. */
    default Validator<Schema> validateEnum() {
        return validateUniqueness()
                .and(validateNameSpace())
                .and(validateSymbols())
                .and(validateSchemaDocumentation())
                .and(validateName());
    }

    /** Validate a record that is defined inline. */
    default Validator<Schema> validateRecord() {
        return validateUniqueness()
                .and(validateNameSpace())
                .and(validateName())
                .and(validateSchemaDocumentation())
                .and(fields(getFieldRules().getValidator(this)));
    }

    /**
     * Validates record schemas of an active source.
     * @return TODO
     */
    default Validator<Schema> validateActiveSource() {
        return validateRecord()
                .and(validateTime()
                .and(validateTimeCompleted())
                .and(validateNotTimeReceived()));
    }

    /**
     * Validates schemas of monitor sources.
     * @return TODO
     */
    default Validator<Schema> validateMonitor() {
        return validateRecord()
                .and(validateTime());
    }

    /**
     * Validates schemas of passive sources.
     */
    default Validator<Schema> validatePassive() {
        return validateRecord()
                .and(validateTime())
                .and(validateTimeReceived())
                .and(validateNotTimeCompleted());
    }

    default Function<Schema, String> messageSchema(String text) {
        return schema -> "Schema " + schema.getFullName() + " is invalid. " + text;
    }

    /**
     * Validates all fields of records.
     * Validation will fail on non-record types or records with no fields.
     */
    default Validator<Schema> fields(Validator<SchemaField> validator) {
        return schema -> {
            if (!schema.getType().equals(Schema.Type.RECORD)) {
                return raise("Default validation can be applied only to an Avro RECORD, not to "
                        + schema.getType() + " of schema " + schema.getFullName() + '.');
            }
            if (schema.getFields().isEmpty()) {
                return raise("Schema " + schema.getFullName() + " does not contain any fields.");
            }
            return schema.getFields().stream()
                    .flatMap(field -> validator.apply(new SchemaField(schema, field)));
        };
    }
}
