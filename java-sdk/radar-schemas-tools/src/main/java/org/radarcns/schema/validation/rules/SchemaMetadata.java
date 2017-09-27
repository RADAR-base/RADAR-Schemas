package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;
import org.radarcns.schema.Scope;

import java.nio.file.Path;

public class SchemaMetadata {
    private final Schema schema;
    private final Scope scope;
    private final Path path;

    public SchemaMetadata(Schema schema) {
        this(schema, null, null);
    }

    public SchemaMetadata(Schema schema, Scope scope, Path path) {
        this.schema = schema;
        this.scope = scope;
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public Scope getScope() {
        return scope;
    }

    public Schema getSchema() {
        return schema;
    }

    public SchemaMetadata withSubSchema(Schema schema) {
        return new SchemaMetadata(schema, scope, path);
    }
}
