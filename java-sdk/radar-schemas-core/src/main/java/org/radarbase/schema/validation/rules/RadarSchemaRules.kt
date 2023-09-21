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
import org.radarbase.schema.validation.ValidationException
import org.radarbase.schema.validation.rules.Validator.Companion.check
import org.radarbase.schema.validation.rules.Validator.Companion.raise
import org.radarbase.schema.validation.rules.Validator.Companion.valid
import org.radarbase.schema.validation.rules.Validator.Companion.validate
import java.util.stream.Stream

/**
 * Schema validation rules enforced for the RADAR-Schemas repository.
 */
class RadarSchemaRules(
    override val fieldRules: RadarSchemaFieldRules = RadarSchemaFieldRules(),
) : SchemaRules {
    val schemaStore: MutableMap<String, Schema> = HashMap()

    override fun validateUniqueness() = Validator { schema: Schema ->
        val key = schema.fullName
        val oldSchema = schemaStore.putIfAbsent(key, schema)
        check(
            oldSchema == null || oldSchema == schema,
            messageSchema(schema, "Schema is already defined elsewhere with a different definition."),
        )
    }

    override fun validateNameSpace() = validate(
        { it.namespace?.matches(NAMESPACE_PATTERN) == true },
        messageSchema("Namespace cannot be null and must fully lowercase, period-separated, without numeric characters."),
    )

    override fun validateName() = validate(
        { it.name?.matches(RECORD_NAME_PATTERN) == true },
        messageSchema("Record names must be camel case."),
    )

    override fun validateSchemaDocumentation() = Validator<Schema> { schema ->
        validateDocumentation(
            schema.doc,
            { m, t -> messageSchema(t, m) },
            schema,
        )
    }

    override fun validateSymbols() = validate(
        { !it.enumSymbols.isNullOrEmpty() },
        messageSchema("Avro Enumerator must have symbol list."),
    ).and(validateSymbolNames())

    private fun validateSymbolNames() = Validator<Schema> { schema ->
        schema.enumSymbols.stream()
            .filter { !it.matches(ENUM_SYMBOL_PATTERN) }
            .map { s ->
                ValidationException(
                    messageSchema(
                        schema,
                        "Symbol $s does not use valid syntax. " +
                            "Enumerator items should be written in uppercase characters separated by underscores.",
                    ),
                )
            }
    }

    /**
     * TODO.
     * @return TODO
     */
    override fun validateTime(): Validator<Schema> = validate(
        { it.getField(TIME)?.schema()?.type == DOUBLE },
        messageSchema("Any schema representing collected data must have a \"$TIME$WITH_TYPE_DOUBLE"),
    )

    /**
     * TODO.
     * @return TODO
     */
    override fun validateTimeCompleted(): Validator<Schema> = validate(
        { it.getField(TIME_COMPLETED)?.schema()?.type == DOUBLE },
        messageSchema("Any ACTIVE schema must have a \"$TIME_COMPLETED$WITH_TYPE_DOUBLE"),
    )

    /**
     * TODO.
     * @return TODO
     */
    override fun validateNotTimeCompleted(): Validator<Schema> = validate(
        { it.getField(TIME_COMPLETED) == null },
        messageSchema("\"$TIME_COMPLETED\" is allow only in ACTIVE schemas."),
    )

    override fun validateTimeReceived(): Validator<Schema> = validate(
        { it.getField(TIME_RECEIVED)?.schema()?.type == DOUBLE },
        messageSchema("Any PASSIVE schema must have a \"$TIME_RECEIVED$WITH_TYPE_DOUBLE"),
    )

    override fun validateNotTimeReceived(): Validator<Schema> = validate(
        { it.getField(TIME_RECEIVED) == null },
        messageSchema("\"$TIME_RECEIVED\" is allow only in PASSIVE schemas."),
    )

    override fun validateAvroData(): Validator<Schema> {
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

    override fun fields(validator: Validator<SchemaField>): Validator<Schema> =
        Validator { schema: Schema ->
            when {
                schema.type != RECORD -> raise(
                    "Default validation can be applied only to an Avro RECORD, not to ${schema.type} of schema ${schema.fullName}.",
                )
                schema.fields.isEmpty() -> raise("Schema ${schema.fullName} does not contain any fields.")
                else -> schema.fields.stream()
                    .flatMap { field -> validator.validate(SchemaField(schema, field)) }
            }
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

        fun <T> validateDocumentation(
            doc: String?,
            message: (String, T) -> String,
            schema: T,
        ): Stream<ValidationException> {
            if (doc.isNullOrEmpty()) {
                return raise(
                    message(
                        "Property \"doc\" is missing. Documentation is" +
                            " mandatory for all fields. The documentation should report what is being" +
                            " measured, how, and what units or ranges are applicable. Abbreviations" +
                            " and acronyms in the documentation should be written out. The sentence" +
                            " must end with a period '.'. Please add \"doc\" property.",
                        schema,
                    ),
                )
            }
            var result: Stream<ValidationException> = valid()
            if (doc[doc.length - 1] != '.') {
                result = raise(
                    message(
                        "Documentation is not terminated with a period. The" +
                            " documentation should report what is being measured, how, and what units" +
                            " or ranges are applicable. Abbreviations and acronyms in the" +
                            " documentation should be written out. Please end the sentence with a" +
                            " period '.'.",
                        schema,
                    ),
                )
            }
            if (!Character.isUpperCase(doc[0])) {
                result = Stream.concat(
                    result,
                    raise(
                        message(
                            "Documentation does not start with a capital letter. The" +
                                " documentation should report what is being measured, how, and what" +
                                " units or ranges are applicable. Abbreviations and acronyms in the" +
                                " documentation should be written out. Please end the sentence with a" +
                                " period '.'.",
                            schema,
                        ),
                    ),
                )
            }
            return result
        }
    }
}
