package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;

public class SchemaField {
    private final SchemaMetadata metadata;
    private final Schema.Field field;

    public SchemaField(SchemaMetadata metadata, Schema.Field field) {
        this.metadata = metadata;
        this.field = field;
    }

    public SchemaMetadata getSchemaMetadata() {
        return metadata;
    }

    public Schema.Field getField() {
        return field;
    }
}
