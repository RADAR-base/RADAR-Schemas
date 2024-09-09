/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.schema.validation.rules

import io.confluent.connect.avro.AvroData
import io.confluent.connect.avro.AvroDataConfig
import io.confluent.connect.avro.AvroDataConfig.Builder
import io.confluent.connect.schema.AbstractDataConfig
import org.apache.avro.Schema
import org.apache.avro.Schema.Type.DOUBLE
import org.apache.avro.Schema.Type.RECORD
import org.radarbase.schema.validation.ValidationContext

/**
 * Schema validation rules enforced for the RADAR-Schemas repository.
 */
class SchemaRules(
    val fieldRules: SchemaFieldRules = SchemaFieldRules(),
) {
    val schemaStore: MutableMap<String, Schema> = HashMap()

    val isUnique = Validator { schema: Schema ->
        val key = schema.fullName
        val oldSchema = schemaStore.putIfAbsent(key, schema)
        if (oldSchema != null && oldSchema != schema) {
            raise(
                schema,
                "Schema is already defined elsewhere with a different definition.",
            )
        }
    }

    val isNamespaceValid = validator(
        predicate = { it.namespace?.matches(NAMESPACE_PATTERN) == true },
        message = schemaErrorMessage("Namespace cannot be null and must fully lowercase, period-separated, without numeric characters."),
    )

    val isNameValid = validator(
        predicate = { it.name?.matches(RECORD_NAME_PATTERN) == true },
        message = schemaErrorMessage("Record names must be camel case."),
    )

    val isDocumentationValid = Validator<Schema> { schema ->
        validateDocumentation(
            schema.doc,
            ValidationContext::raise,
            schema,
        )
    }

    val isEnumSymbolsValid = Validator<Schema> { schema ->
        if (schema.enumSymbols.isNullOrEmpty()) {
            raise(schema, "Avro Enumerator must have symbol list.")
            return@Validator
        }
        schema.enumSymbols.forEach { s ->
            if (!s.matches(ENUM_SYMBOL_PATTERN)) {
                raise(
                    schema,
                    "Symbol $s does not use valid syntax. " +
                        "Enumerator items should be written in uppercase characters separated by underscores.",
                )
            }
        }
    }

    val hasTime: Validator<Schema> = validator(
        predicate = { it.getField(TIME)?.schema()?.type == DOUBLE },
        message = schemaErrorMessage("Any schema representing collected data must have a \"$TIME$WITH_TYPE_DOUBLE"),
    )

    val hasTimeCompleted: Validator<Schema> = validator(
        predicate = { it.getField(TIME_COMPLETED)?.schema()?.type == DOUBLE },
        message = schemaErrorMessage("Any ACTIVE schema must have a \"$TIME_COMPLETED$WITH_TYPE_DOUBLE"),
    )

    val hasNoTimeCompleted: Validator<Schema> = validator(
        predicate = { it.getField(TIME_COMPLETED) == null },
        message = schemaErrorMessage("\"$TIME_COMPLETED\" is allow only in ACTIVE schemas."),
    )

    val hasTimeReceived: Validator<Schema> = validator(
        predicate = { it.getField(TIME_RECEIVED)?.schema()?.type == DOUBLE },
        message = schemaErrorMessage("Any PASSIVE schema must have a \"$TIME_RECEIVED$WITH_TYPE_DOUBLE"),
    )

    val hasNoTimeReceived: Validator<Schema> = validator(
        predicate = { it.getField(TIME_RECEIVED) == null },
        message = schemaErrorMessage("\"$TIME_RECEIVED\" is allow only in PASSIVE schemas."),
    )

    /**
     * Validate an enum.
     */
    val isEnumValid: Validator<Schema> = all(
        isUnique,
        isNamespaceValid,
        isEnumSymbolsValid,
        isDocumentationValid,
        isNameValid,
    )

    /**
     * Validate a record that is defined inline.
     */
    val isRecordValid: Validator<Schema>

    /**
     * Validates record schemas of an active source.
     */
    val isActiveSourceValid: Validator<Schema>

    /**
     * Validates schemas of monitor sources.
     */
    val isMonitorSourceValid: Validator<Schema>

    /**
     * Validates schemas of passive sources.
     */
    val isPassiveSourceValid: Validator<Schema>

    init {
        fieldRules.schemaRules = this

        isRecordValid = all(
            isUnique,
            isAvroConnectCompatible(),
            isNamespaceValid,
            isNameValid,
            isDocumentationValid,
            isFieldsValid(fieldRules.isFieldValid),
        )

        isActiveSourceValid = all(isRecordValid, hasTime)

        isMonitorSourceValid = all(isRecordValid, hasTime)

        isPassiveSourceValid = all(isRecordValid, hasTime, hasTimeReceived, hasNoTimeCompleted)
    }

    fun isFieldsValid(validator: Validator<SchemaField>): Validator<Schema> =
        Validator { schema: Schema ->
            when {
                schema.type != RECORD -> raise(
                    "Default validation can be applied only to an Avro RECORD, not to ${schema.type} of schema ${schema.fullName}.",
                )
                schema.fields.isEmpty() -> raise("Schema ${schema.fullName} does not contain any fields.")
                else -> validator.validateAll(schema.fields.map { SchemaField(schema, it) })
            }
        }

    private fun isAvroConnectCompatible(): Validator<Schema> {
        val avroConfig = Builder()
            .with(AvroDataConfig.CONNECT_META_DATA_CONFIG, false)
            .with(AbstractDataConfig.SCHEMAS_CACHE_SIZE_CONFIG, 10)
            .with(AvroDataConfig.ENHANCED_AVRO_SCHEMA_SUPPORT_CONFIG, true)
            .build()

        return Validator { schema: Schema ->
            val encoder = AvroData(10)
            val decoder = AvroData(avroConfig)
            try {
                val connectSchema = encoder.toConnectSchema(schema)
                val originalSchema = decoder.fromConnectSchema(connectSchema)
                check(schema == originalSchema) {
                    "Schema changed by validation: " +
                        schema.toString(true) + " is not equal to " +
                        originalSchema.toString(true)
                }
            } catch (ex: Exception) {
                raise("Failed to convert schema back to itself")
            }
        }
    }

    private fun schemaErrorMessage(text: String): (Schema) -> String {
        return { schema -> "Schema ${schema.fullName} is invalid. $text" }
    }

    companion object {
        // used in testing
        const val TIME = "time"
        private const val TIME_RECEIVED = "timeReceived"
        private const val TIME_COMPLETED = "timeCompleted"

        val NAMESPACE_PATTERN = "[a-z]+(\\.[a-z]+)*".toRegex()

        // CamelCase
        // see SchemaValidatorRolesTest#recordNameRegex() for valid and invalid values
        val RECORD_NAME_PATTERN = "([A-Z]([a-z]*[0-9]*))+[A-Z]?".toRegex()

        // used in testing
        val ENUM_SYMBOL_PATTERN = "[A-Z][A-Z0-9_]*".toRegex()
        private const val WITH_TYPE_DOUBLE = "\" field with type \"double\"."

        fun <T> ValidationContext.validateDocumentation(
            doc: String?,
            raise: ValidationContext.(T, String) -> Unit,
            schema: T,
        ) {
            if (doc.isNullOrEmpty()) {
                raise(
                    schema,
                    """Property "doc" is missing. Documentation is mandatory for all fields.
                        | The documentation should report what is being measured, how, and what
                        |  units or ranges are applicable. Abbreviations and acronyms in the
                        |   documentation should be written out. The sentence must end with a
                        |    period '.'. Please add "doc" property.
                    """.trimMargin(),
                )
                return
            }
            if (doc[doc.length - 1] != '.') {
                raise(
                    schema,
                    "Documentation is not terminated with a period. The" +
                        " documentation should report what is being measured, how, and what units" +
                        " or ranges are applicable. Abbreviations and acronyms in the" +
                        " documentation should be written out. Please end the sentence with a" +
                        " period '.'.",
                )
            }
            if (!Character.isUpperCase(doc[0])) {
                raise(
                    schema,
                    "Documentation does not start with a capital letter. The" +
                        " documentation should report what is being measured, how, and what" +
                        " units or ranges are applicable. Abbreviations and acronyms in the" +
                        " documentation should be written out. Please end the sentence with a" +
                        " period '.'.",
                )
            }
        }
    }
}

fun ValidationContext.raise(schema: Schema, text: String) {
    raise("Schema ${schema.fullName} is invalid. $text")
}
