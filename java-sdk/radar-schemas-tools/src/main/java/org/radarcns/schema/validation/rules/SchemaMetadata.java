package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;
import org.radarcns.schema.Scope;

import java.nio.file.Path;

/**
 * Schema with metadata.
 */
public class SchemaMetadata {
    private final Schema schema;
    private final Scope scope;
    private final Path path;

    /**
     * Schema with {@code null} metadata.
     */
    public SchemaMetadata(Schema schema) {
        this(schema, null, null);
    }

    /**
     * Schema with metadata.
     */
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
}
