package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Field

data class SchemaField(
    val schema: Schema,
    val field: Field,
)
