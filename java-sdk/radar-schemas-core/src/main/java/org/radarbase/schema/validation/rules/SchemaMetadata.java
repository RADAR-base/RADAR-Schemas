package org.radarbase.schema.validation.rules;

import java.nio.file.Path;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchemaMetadata that = (SchemaMetadata) o;

        return scope == that.scope
                && Objects.equals(path, that.path)
                && Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        int result = scope != null ? scope.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
