package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

import java.util.function.Function;

import static org.radarcns.schema.validation.rules.Validator.raise;
import static org.radarcns.schema.validation.rules.Validator.valid;

public interface SchemaFieldRules {
    default Validator<SchemaField> getValidator(SchemaRules schemaRules) {
        return validateFieldTypes(schemaRules)
                .and(validateFieldName())
                .and(validateDefault())
                .and(validateFieldDocumentation());
    }

    /** Recursively checks field types. */
    Validator<SchemaField> validateFieldTypes(SchemaRules schemaRules);

    /** Checks field name format. */
    Validator<SchemaField> validateFieldName();

    /** Checks field documentation presence and format. */
    Validator<SchemaField> validateFieldDocumentation();

    Validator<SchemaField> validateDefault();

    default Validator<SchemaField> validateInternalUnion(SchemaRules schemaRules) {
        return field -> field.getField().schema().getTypes().stream()
                .flatMap(schema -> {
                    Schema.Type type = schema.getType();
                    if (type == Schema.Type.RECORD) {
                        return schemaRules.validateRecord(false).apply(schema);
                    } else if (type == Schema.Type.ENUM) {
                        return schemaRules.validateEnum(false).apply(schema);
                    } else if (type == Schema.Type.UNION) {
                        return raise(messageField("Cannot have a nested union.")
                                .apply(field));
                    } else {
                        return valid();
                    }
                });
    }

    default Function<SchemaField, String> messageField(String text) {
        return schema -> "Field " + schema.getField().name() + " in schema "
                + schema.getSchema().getFullName() + " is invalid. " + text;
    }
}
