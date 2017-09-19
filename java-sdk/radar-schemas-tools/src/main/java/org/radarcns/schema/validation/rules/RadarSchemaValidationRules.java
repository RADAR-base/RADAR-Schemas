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

import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.ValidationException;
import org.radarcns.schema.validation.ValidationSupport;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.radarcns.schema.validation.rules.Validator.matches;
import static org.radarcns.schema.validation.rules.Validator.raise;
import static org.radarcns.schema.validation.rules.Validator.valid;
import static org.radarcns.schema.validation.rules.Validator.validate;
import static org.radarcns.schema.validation.rules.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * Schema validation rules enforced for the RADAR-Schemas repository.
 */
public class RadarSchemaValidationRules implements SchemaValidationRules {

    private static final String UNKNOWN = "UNKNOWN";
    static final String TIME = "time";
    private static final String TIME_RECEIVED = "timeReceived";
    private static final String TIME_COMPLETED = "timeCompleted";
    private final Map<Type, Function<SchemaField, Stream<ValidationException>>>
            defaultsValidator;

    static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z]+(\\.[a-z]+)*$");

    // CamelCase
    // see SchemaValidatorRolesTest#recordNameRegex() for valid and invalid values
    static final Pattern RECORD_NAME_PATTERN = Pattern.compile(
            "^([A-Z]([a-z]+[0-9]*|[a-z]*[0-9]+))+[A-Z]?$");

    // lowerCamelCase
    static final Pattern FIELD_NAME_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9]*([a-z0-9][A-Z][a-z0-9]+)?([A-Z][a-z0-9]+)*[A-Z]?$");

    static final Pattern ENUM_SYMBOL_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    private static final List<String> FIELD_NAME_NOT_ALLOWED_SUFFIX = Arrays.asList(
            "value", "Value");

    private static final String NAME_SPACE = "Namespace cannot be null and must fully lowercase dot"
            + " separated without numeric. In this case the expected value is \"";
    private static final String TIME_FIELD = "Any schema representing collected data must have a \""
            + TIME + "\" field formatted in " + Type.DOUBLE + ".";
    private static final String TIME_COMPLETED_FIELD = "Any " + Scope.ACTIVE
            + " schema must have a \"" + TIME_COMPLETED + "\" field formatted in "
            + Type.DOUBLE + ".";
    private static final String NOT_TIME_COMPLETED_FIELD = "\"" + TIME_COMPLETED
            + "\" is allow only in " + Scope.ACTIVE + " schemas.";
    private static final String TIME_RECEIVED_FIELD = "Any " + Scope.PASSIVE
            + " schema must have a \"" + TIME_RECEIVED + "\" field formatted in "
            + Type.DOUBLE + ".";
    private static final String NOT_TIME_RECEIVED_FIELD = "\"" + TIME_RECEIVED
            + "\" is allow only in " + Scope.PASSIVE + " schemas.";
    private static final String FIELD_NAME_NOT_ALLOWED = "Field name cannot end with the"
            + " following values " + FIELD_NAME_NOT_ALLOWED_SUFFIX + ".";
    private static final String FIELD_NAME_LOWER_CAMEL = "Field name does not respect"
            + " lowerCamelCase name convention. Please avoid abbreviations and write out the"
            + " field name instead.";
    private static final String SYMBOLS = "Avro Enumerator must have symbol list.";
    private static final String ENUMERATION_SYMBOL = "Enumerator items should be written in"
            + " uppercase characters separated by underscores.";

    private final Path root;
    private final ExcludeConfig config;

    /**
     * RADAR-Schema validation rules.
     * @param root root directory of the RADAR-Schemas repository
     * @param config validation configuration
     */
    public RadarSchemaValidationRules(Path root, ExcludeConfig config) {
        this.root = root;
        this.config = config;

        defaultsValidator = new HashMap<>();
        defaultsValidator.put(Type.RECORD, validateDefault());
        defaultsValidator.put(Type.ENUM, this::validateDefaultEnum);
        defaultsValidator.put(Type.UNION, this::validateDefaultUnion);
    }

    @Override
    public Validator<SchemaField> validateFieldTypes() {
        return field -> {
            SchemaMetadata metadata = field.getSchemaMetadata().withSubSchema(
                    field.getField().schema());

            Schema.Type subType = field.getField().schema().getType();
            if (subType == Schema.Type.UNION) {
                return validateInternalUnion(field);
            } else if (subType == Schema.Type.RECORD) {
                return internalRecordValidation().apply(metadata);
            } else if (subType == Schema.Type.ENUM) {
                return internalEnumValidation().apply(metadata);
            } else {
                return valid();
            }
        };
    }

    private Stream<ValidationException> validateInternalUnion(SchemaField field) {
        return field.getField().schema().getTypes().stream()
                .flatMap(schema -> {
                    Schema.Type type = schema.getType();
                    SchemaMetadata subMeta = field.getSchemaMetadata().withSubSchema(
                            schema);
                    if (type == Schema.Type.RECORD) {
                        return internalRecordValidation().apply(subMeta);
                    } else if (type == Schema.Type.ENUM) {
                        return internalEnumValidation().apply(subMeta);
                    } if (type == Schema.Type.UNION) {
                        return raise(messageField("Cannot have a nested union.")
                                .apply(field));
                    } else {
                        return valid();
                    }
                });
    }

    @Override
    public Validator<SchemaMetadata> validateSchemaLocation() {
        return validateNamespaceSchemaLocation()
                .and(validateNameSchemaLocation());
    }

    private Validator<SchemaMetadata> validateNamespaceSchemaLocation() {
        return metadata -> {
                try {
                    String expected = ValidationSupport.getNamespace(
                            root, metadata.getPath(), metadata.getScope());
                    String namespace = metadata.getSchema().getNamespace();

                    return expected.equalsIgnoreCase(namespace) ? valid() : raise(message(
                            NAME_SPACE + expected + "\".").apply(metadata));
                } catch (IllegalArgumentException ex) {
                    return Stream.of(new ValidationException("Path " + metadata.getPath()
                            + " is not part of root " + root, ex));
                }
        };
    }

    private Validator<SchemaMetadata> validateNameSchemaLocation() {
        return metadata -> {
            String expected = ValidationSupport.getRecordName(metadata.getPath());

            return expected.equalsIgnoreCase(metadata.getSchema().getName()) ? valid() : raise(
                    message("Record name should match file name. Expected record name is \""
                                + expected + "\".").apply(metadata));
        };
    }

    /**
     * TODO.
     * @return TODO
     */
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

    private <T> Stream<ValidationException> validateDocumentation(String doc,
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
    public Validator<SchemaField> validateFieldName() {
        return validateFieldName(config::isSkipped);
    }

    /**
     * Checks field names, except when the given predicate tests true.
     * @param skip fields to skip
     */
    protected Validator<SchemaField> validateFieldName(Predicate<SchemaField> skip) {
        return field -> {
            if (!skip.test(field)) {
                String name = field.getField().name();
                if (!matches(name, FIELD_NAME_PATTERN)) {
                    return raise(messageField(FIELD_NAME_LOWER_CAMEL).apply(field));
                }
                if (FIELD_NAME_NOT_ALLOWED_SUFFIX.stream().anyMatch(name::endsWith)) {
                    return raise(messageField(FIELD_NAME_NOT_ALLOWED).apply(field));
                }
            }
            return valid();
        };
    }

    @Override
    public Validator<SchemaField> validateFieldDocumentation() {
        return field -> validateDocumentation(field.getField().doc(),
                (m, f) -> messageField(m).apply(f), field);
    }

    @Override
    public Validator<Schema> validateSymbols() {
        return validateNonEmpty(Schema::getEnumSymbols, messageSchema(SYMBOLS))
                .and(schema -> schema.getEnumSymbols().stream()
                        .filter(symbol -> !matches(symbol, ENUM_SYMBOL_PATTERN))
                        .map(s -> new ValidationException(messageSchema(
                                "Symbol " + s + " does not use valid syntax. "
                                        + ENUMERATION_SYMBOL).apply(schema))));
    }

    @Override
    public Validator<SchemaField> validateDefault() {
        return input -> defaultsValidator
                        .getOrDefault(input.getField().schema().getType(), this::validateDefaultOther)
                        .apply(input);
    }

    private Stream<ValidationException> validateDefaultEnum(SchemaField field) {
        return !field.getField().schema().getEnumSymbols().contains(UNKNOWN)
                    || (field.getField().defaultVal() != null
                    && field.getField().defaultVal().toString().equals(UNKNOWN)) ? valid()
                    : raise(messageField("Default is \"" + field.getField().defaultVal()
                            + "\". Any Avro enum type that has an \"UNKNOWN\" symbol must set its"
                            + " default value to \"UNKNOWN\".").apply(field));
    }

    private Stream<ValidationException> validateDefaultUnion(SchemaField field) {
        return !field.getField().schema().getTypes().contains(Schema.create(Type.NULL))
                || (field.getField().defaultVal() != null
                && field.getField().defaultVal().equals(JsonProperties.NULL_VALUE)) ? valid()
                : raise(messageField("Default is not null. Any nullable Avro field must"
                + " specify have its default value set to null.").apply(field));
    }

    private Stream<ValidationException> validateDefaultOther(SchemaField field) {
        return field.getField().defaultVal() == null ? valid() : raise(messageField(
                "Default of type " + field.getField().schema().getType() + " is set to "
                + field.getField().defaultVal() + ". The only acceptable default values are the"
                + " \"UNKNOWN\" enum symbol and null.").apply(field));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTime() {
        return validateNonNull(s -> s.getField(TIME),
                time -> time.schema().getType().equals(Type.DOUBLE), messageSchema(TIME_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTimeCompleted() {
        return validateNonNull(s -> s.getField(TIME_COMPLETED),
                time -> time.schema().getType().equals(Type.DOUBLE), messageSchema(TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            messageSchema(NOT_TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTimeReceived() {
        return validateNonNull(s -> s.getField(TIME_RECEIVED),
                time -> time.schema().getType().equals(Type.DOUBLE), messageSchema(TIME_RECEIVED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
                messageSchema(NOT_TIME_RECEIVED_FIELD));
    }

    private static Function<Schema, String> messageSchema(String text) {
        return schema -> "Schema " + schema.getFullName() + " is invalid. " + text;
    }

    private static Function<SchemaMetadata, String> message(String text) {
        return metadata -> "Schema " + metadata.getSchema().getFullName()
                + " at " + metadata.getPath() + " is invalid. " + text;
    }

    private static Function<SchemaField, String> messageField(String text) {
        return schema -> "Field " + schema.getField().name() + " in schema "
                + schema.getSchemaMetadata().getSchema().getFullName() + " is invalid. " + text;
    }

    @Override
    public Validator<SchemaMetadata> fields(Validator<SchemaField> validator) {
        return metadata -> {
            Schema schema = metadata.getSchema();
            if (config.skipFile(metadata.getPath())) {
                return valid();
            }
            if (!schema.getType().equals(Schema.Type.RECORD)) {
                return raise("Default validation can be applied only to an Avro RECORD, not to "
                        + schema.getType() + " of schema " + schema.getFullName() + '.');
            }
            if (schema.getFields().isEmpty()) {
                return raise("Schema " + schema.getFullName() + " does not contain any fields.");
            }
            return schema.getFields().stream()
                    .flatMap(field -> {
                        SchemaField schemaField = new SchemaField(metadata, field);
                        return config.isSkipped(schemaField) ? valid()
                                : validator.apply(schemaField);
                    });
        };
    }

    @Override
    public Validator<SchemaMetadata> schema(Validator<Schema> validator) {
        return metadata -> config.skipFile(metadata.getPath()) ? valid()
                : validator.apply(metadata.getSchema());
    }
}
