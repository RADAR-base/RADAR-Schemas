package org.radarbase.schema.validation.rules

import org.apache.avro.Schema
import org.apache.avro.Schema.Field

data class SchemaField(@JvmField val schema: Schema, @JvmField val field: Field)
