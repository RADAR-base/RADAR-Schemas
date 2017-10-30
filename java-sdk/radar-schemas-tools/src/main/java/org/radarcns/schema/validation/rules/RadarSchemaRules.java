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

package org.radarcns.schema.validation.rules;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.radarcns.schema.validation.rules.Validator.check;
import static org.radarcns.schema.validation.rules.Validator.matches;
import static org.radarcns.schema.validation.rules.Validator.raise;
import static org.radarcns.schema.validation.rules.Validator.valid;
import static org.radarcns.schema.validation.rules.Validator.validate;
import static org.radarcns.schema.validation.rules.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * Schema validation rules enforced for the RADAR-Schemas repository.
 */
public class RadarSchemaRules implements SchemaRules {

    static final String TIME = "time";
    private static final String TIME_RECEIVED = "timeReceived";
    private static final String TIME_COMPLETED = "timeCompleted";

    static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z]+(\\.[a-z]+)*$");
    private final Map<String, Schema> schemaStore;

    // CamelCase
    // see SchemaValidatorRolesTest#recordNameRegex() for valid and invalid values
    static final Pattern RECORD_NAME_PATTERN = Pattern.compile(
            "^([A-Z]([a-z]+[0-9]*|[a-z]*[0-9]+))+[A-Z]?$");

    static final Pattern ENUM_SYMBOL_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    private static final String WITH_TYPE_DOUBLE = "\" field with type \"double\".";

    private final ExcludeConfig config;
    private final RadarSchemaFieldRules fieldRules;

    /**
     * RADAR-Schema validation rules.
     * @param config validation configuration
     */
    public RadarSchemaRules(ExcludeConfig config, RadarSchemaFieldRules fieldRules) {
        this.config = config;
        this.fieldRules = fieldRules;
        this.schemaStore = new HashMap<>();
    }

    public RadarSchemaRules(ExcludeConfig config) {
        this(config, new RadarSchemaFieldRules());
    }

    @Override
    public SchemaFieldRules getFieldRules() {
        return fieldRules;
    }

    @Override
    public Validator<Schema> validateUniqueness() {
        return schema -> {
            String key = schema.getFullName();
            Schema oldSchema = schemaStore.putIfAbsent(key, schema);
            return check(oldSchema == null || oldSchema.equals(schema), messageSchema(
                    "Schema is already defined elsewhere with a different definition.")
                    .apply(schema));
        };
    }

    @Override
    public Validator<Schema> validateNameSpace() {
        return validateNonNull(Schema::getNamespace, matches(NAMESPACE_PATTERN),
                messageSchema("Namespace cannot be null and must fully lowercase, period-"
                + "separated, without numeric characters."));
    }

    @Override
    public Validator<Schema> validateName() {
        return validateNonNull(Schema::getName, matches(RECORD_NAME_PATTERN),
                messageSchema("Record names must be camel case."));
    }

    @Override
    public Validator<Schema> validateSchemaDocumentation() {
        return schema -> validateDocumentation(schema.getDoc(), (m, t) -> messageSchema(m).apply(t),
                schema);
    }

    static <T> Stream<ValidationException> validateDocumentation(String doc,
            BiFunction<String, T, String> message, T schema) {
        if (doc == null || doc.isEmpty()) {
            return raise(message.apply("Property \"doc\" is missing. Documentation is"
                    + " mandatory for all fields. The documentation should report what is being"
                    + " measured, how, and what units or ranges are applicable. Abbreviations"
                    + " and acronyms in the documentation should be written out. The sentence"
                    + " must end with a period '.'. Please add \"doc\" property.", schema));
        }

        Stream<ValidationException> result = valid();
        if (doc.charAt(doc.length() - 1) != '.') {
            result = raise(message.apply("Documentation is not terminated with a period. The"
                    + " documentation should report what is being measured, how, and what units"
                    + " or ranges are applicable. Abbreviations and acronyms in the"
                    + " documentation should be written out. Please end the sentence with a"
                    + " period '.'.", schema));
        }
        if (!Character.isUpperCase(doc.charAt(0))) {
            result = Stream.concat(result, raise(
                    message.apply("Documentation does not start with a capital letter. The"
                            + " documentation should report what is being measured, how, and what"
                            + " units or ranges are applicable. Abbreviations and acronyms in the"
                            + " documentation should be written out. Please end the sentence with a"
                            + " period '.'.", schema)));
        }
        return result;
    }


    @Override
    public Validator<Schema> validateSymbols() {
        return validateNonEmpty(Schema::getEnumSymbols, messageSchema(
                "Avro Enumerator must have symbol list."))
                .and(schema -> schema.getEnumSymbols().stream()
                        .filter(symbol -> !matches(symbol, ENUM_SYMBOL_PATTERN))
                        .map(s -> new ValidationException(messageSchema(
                                "Symbol " + s + " does not use valid syntax. "
                                    + "Enumerator items should be written in"
                                    + " uppercase characters separated by underscores.")
                                .apply(schema))));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTime() {
        return validateNonNull(s -> s.getField(TIME),
                time -> time.schema().getType().equals(Type.DOUBLE), messageSchema(
                        "Any schema representing collected data must have a \""
                        + TIME + WITH_TYPE_DOUBLE));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTimeCompleted() {
        return validateNonNull(s -> s.getField(TIME_COMPLETED),
                time -> time.schema().getType().equals(Type.DOUBLE),
                messageSchema("Any ACTIVE schema must have a \""
                        + TIME_COMPLETED + WITH_TYPE_DOUBLE));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateNotTimeCompleted() {
        return validate(s -> s.getField(TIME_COMPLETED), Objects::isNull,
            messageSchema("\"" + TIME_COMPLETED
                    + "\" is allow only in ACTIVE schemas."));
    }

    @Override
    public Validator<Schema> validateTimeReceived() {
        return validateNonNull(s -> s.getField(TIME_RECEIVED),
                time -> time.schema().getType().equals(Type.DOUBLE),
                messageSchema("Any PASSIVE schema must have a \""
                        + TIME_RECEIVED + WITH_TYPE_DOUBLE));
    }

    @Override
    public Validator<Schema> validateNotTimeReceived() {
        return validate(s -> s.getField(TIME_RECEIVED), Objects::isNull,
                messageSchema("\"" + TIME_RECEIVED + "\" is allow only in PASSIVE schemas."));
    }

    @Override
    public Validator<Schema> fields(Validator<SchemaField> validator) {
        return schema -> {
            if (!schema.getType().equals(Schema.Type.RECORD)) {
                return raise("Default validation can be applied only to an Avro RECORD, not to "
                        + schema.getType() + " of schema " + schema.getFullName() + '.');
            }
            if (schema.getFields().isEmpty()) {
                return raise("Schema " + schema.getFullName() + " does not contain any fields.");
            }
            return schema.getFields().stream()
                    .flatMap(field -> {
                        SchemaField schemaField = new SchemaField(schema, field);
                        return config.isSkipped(schemaField) ? valid()
                                : validator.apply(schemaField);
                    });
        };
    }

    public Map<String, Schema> getSchemaStore() {
        return Collections.unmodifiableMap(schemaStore);
    }
}
