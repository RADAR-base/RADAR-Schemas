package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.radarbase.schema.Scope
import java.nio.file.Path

/**
 * Schema with metadata.
 */
data class SchemaMetadata(
    val schema: Schema,
    val scope: Scope,
    val path: Path,
)

data class FailedSchemaMetadata(
    val scope: Scope,
    val path: Path,
)
