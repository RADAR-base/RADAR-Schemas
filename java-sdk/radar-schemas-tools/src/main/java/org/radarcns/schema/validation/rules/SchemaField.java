package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

public class SchemaField {
    private final Schema schema;
    private final Schema.Field field;

    public SchemaField(Schema schema, Schema.Field field) {
        this.schema = schema;
        this.field = field;
    }

    public Schema getSchema() {
        return schema;
    }

    public Schema.Field getField() {
        return field;
    }
}
