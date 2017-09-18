package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

public interface SchemaValidationRules {
    Validator<SchemaMetadata> validateNameSpace();

    Validator<SchemaMetadata> validateRecordName();

    Validator<Schema> validateSchemaDocumentation();

    Validator<Schema> validateFields();

    Validator<Schema> validateFieldName();

    Validator<Schema> validateFieldDocumentation();

    Validator<Schema> validateSymbols();

    Validator<Schema> validateEnumerationSymbols();

    Validator<Schema> validateDefault();

    Validator<Schema> validateTime();

    Validator<Schema> validateTimeCompleted();

    Validator<Schema> validateNotTimeCompleted();

    Validator<Schema> validateTimeReceived();

    Validator<Schema> validateNotTimeReceived();

    /**
     * TODO.
     * @return TODO
     */
    default Validator<SchemaMetadata> generalRecordValidation() {
        return validateNameSpace()
                .and(validateRecordName())
                .and(validateSchemaDocumentation(), SchemaMetadata::getSchema)
                .and(validateFields(), SchemaMetadata::getSchema)
                .and(validateFieldName(), SchemaMetadata::getSchema)
                .and(validateDefault(), SchemaMetadata::getSchema)
                .and(validateFieldDocumentation(), SchemaMetadata::getSchema)
                .and(validateEnumerationSymbols(), SchemaMetadata::getSchema);
    }


    /**
     * TODO.
     * @return TODO
     */
    default Validator<SchemaMetadata> validateActive() {
        return generalRecordValidation()
                .and(validateTime(), SchemaMetadata::getSchema)
                .and(validateTimeCompleted(), SchemaMetadata::getSchema)
                .and(validateNotTimeReceived(), SchemaMetadata::getSchema);
    }

    /**
     * TODO.
     * @return TODO
     */
    default Validator<SchemaMetadata> validateMonitor() {
        return generalRecordValidation()
                .and(validateTime(), SchemaMetadata::getSchema);
    }

    /**
     * TODO.
     * @return TODO
     */
    default Validator<SchemaMetadata> validatePassive() {
        return generalRecordValidation()
                .and(validateTime(), SchemaMetadata::getSchema)
                .and(validateTimeReceived(), SchemaMetadata::getSchema)
                .and(validateNotTimeCompleted(), SchemaMetadata::getSchema);
    }

    /**
     * TODO.
     * @return TODO
     */
    default Validator<SchemaMetadata> validateEnum() {
        return validateNameSpace()
                .and(validateRecordName())
                .and(validateSchemaDocumentation(), SchemaMetadata::getSchema)
                .and(validateSymbols(), SchemaMetadata::getSchema)
                .and(validateEnumerationSymbols(), SchemaMetadata::getSchema);
    }

    default Validator<SchemaMetadata> getValidator() {
        return schema -> {
            if (schema.getSchema().getType().equals(Schema.Type.ENUM)) {
                return validateEnum().apply(schema);
            } else {
                switch (schema.getScope()) {
                    case ACTIVE:
                        return validateActive().apply(schema);
                    case CATALOGUE:
                        return generalRecordValidation().apply(schema);
                    case KAFKA:
                        return generalRecordValidation().apply(schema);
                    case MONITOR:
                        return validateMonitor().apply(schema);
                    case PASSIVE:
                        return validatePassive().apply(schema);
                    default:
                        return generalRecordValidation().apply(schema);
                }
            }
        };
    }
}
