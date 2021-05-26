package org.radarbase.schema.validation.rules;

import java.nio.file.Path;
import org.apache.avro.Schema;
import org.radarbase.schema.Scope;

/**
 * Schema with metadata.
 */
public class SchemaMetadata {
    private final Schema schema;
    private final Scope scope;
    private final Path path;

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
