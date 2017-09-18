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
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.radarcns.schema.validation.ValidationSupport.extractEnumerationFields;
import static org.radarcns.schema.validation.ValidationSupport.nonEmpty;
import static org.radarcns.schema.validation.rules.Validator.matches;
import static org.radarcns.schema.validation.rules.Validator.raise;
import static org.radarcns.schema.validation.rules.Validator.valid;
import static org.radarcns.schema.validation.rules.Validator.validate;
import static org.radarcns.schema.validation.rules.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public class RadarSchemaValidationRules implements SchemaValidationRules {

    static final String TIME = "time";
    private static final String TIME_RECEIVED = "timeReceived";
    private static final String TIME_COMPLETED = "timeCompleted";
    private final Map<Type, BiFunction<Schema.Field, Schema, Stream<ValidationException>>>
            defaultsValidator;

    public static final String UNKNOWN = "UNKNOWN";

    static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z.]*$");

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
    private static final String FIELDS = "Avro Record must have field list.";
    private static final String FIELD_NAME_NOT_ALLOWED = "Field name cannot end with the"
            + " following values " + FIELD_NAME_NOT_ALLOWED_SUFFIX + ".";
    private static final String FIELD_NAME_LOWER_CAMEL = "Field name does not respect"
            + " lowerCamelCase name convention. Please avoid abbreviations and write out the"
            + " field name instead.";
    private static final String DOC = "Documentation is mandatory for any schema and field."
            + " The documentation should report "
            + "what is being measured, how, and what units or ranges are applicable. Abbreviations "
            + "and acronyms in the documentation should be written out. The sentence must be ended "
            + "by a point. Please add \"doc\" property.";
    private static final String SYMBOLS = "Avro Enumerator must have symbol list.";
    private static final String ENUMERATION_SYMBOL = "Enumerator items should be written in"
            + " uppercase characters separated by underscores.";

    private final Path root;
    private final ExcludeConfig config;

    public RadarSchemaValidationRules(Path root, ExcludeConfig config) {
        this.root = root;
        this.config = config;

        defaultsValidator = new HashMap<>();
        defaultsValidator.put(Type.RECORD, (f, s) -> this.validateDefault().apply(f.schema()));
        defaultsValidator.put(Type.ENUM, this::validateDefaultEnum);
        defaultsValidator.put(Type.UNION, this::validateDefaultUnion);
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<SchemaMetadata> validateNameSpace() {
        return metadata -> {
            String expected = ValidationSupport.getNamespace(root,
                    metadata.getPath(), metadata.getScope());

            String namespace = metadata.getSchema().getNamespace();

            return namespace != null && matches(namespace, NAMESPACE_PATTERN)
                    && namespace.equalsIgnoreCase(expected) ? valid() : raise(
                            message(NAME_SPACE).apply(metadata.getSchema())
                                    + expected + "\".");
        };
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<SchemaMetadata> validateRecordName() {
        return metadata -> {
            String name = metadata.getSchema().getName();

            if (!config.skipFile(metadata.getPath())) {
                if (!matches(name, RECORD_NAME_PATTERN)) {
                    return raise(message("Record names must be camel case.")
                            .apply(metadata.getSchema()));
                }
                String expected = ValidationSupport.getRecordName(metadata.getPath());
                if (!name.equalsIgnoreCase(expected)) {
                    return raise(message("The path of a record must match its schema."
                            + " Expected record name is \"")
                            .apply(metadata.getSchema()) + expected + "\".");
                }
            }
            return valid();
        };
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateSchemaDocumentation() {
        return validateNonNull(Schema::getDoc, doc -> doc.charAt(doc.length() - 1) == '.',
                message(DOC));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateFields() {
        return validateNonEmpty(Schema::getFields, message(FIELDS));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateFieldName() {
        return validateFieldName(config::skippedNameFieldCheck);
    }

    /**
     * TODO.
     * @return TODO
     */
    protected Validator<Schema> validateFieldName(Function<Schema, Set<String>> skip) {
        return schema -> {
            Stream<String> stream = schema.getFields().stream()
                    .map(Schema.Field::name);
            Set<String> skipped = skip.apply(schema);
            if (skipped != null && !skipped.isEmpty()) {
                stream = stream.filter(name -> !skipped.contains(name));
            }

            return stream
                    .flatMap(name -> {
                        if (!matches(name, FIELD_NAME_PATTERN)) {
                            return raise(
                                    "Field " + name + " of schema " + schema.getFullName()
                                            + " is invalid. " + FIELD_NAME_LOWER_CAMEL);
                        }
                        if (FIELD_NAME_NOT_ALLOWED_SUFFIX.stream().anyMatch(name::endsWith)) {
                            return raise(
                                    "Field " + name + " of schema " + schema.getFullName()
                                            + " is invalid. " + FIELD_NAME_NOT_ALLOWED);
                        }
                        return valid();
                    });
        };
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateFieldDocumentation() {
        return validate(schema ->
                    schema.getFields()
                            .stream()
                            .allMatch(field -> field.doc() != null
                                    && field.doc().charAt(field.doc().length() - 1) == '.'),
            message(DOC));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateSymbols() {
        return validate(schema -> nonEmpty(schema.getEnumSymbols()), message(SYMBOLS));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateEnumerationSymbols() {
        return validate(schema ->
                extractEnumerationFields(schema).stream().allMatch(matches(ENUM_SYMBOL_PATTERN)),
            message(ENUMERATION_SYMBOL));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateDefault() {
        return input -> {
            if (!input.getType().equals(Type.RECORD)) {
                return raise("Default validation can be applied only to an Avro RECORD, not to "
                        + input.getType() + '.');
            }

            return input.getFields().stream()
                    .flatMap(field -> defaultsValidator
                            .getOrDefault(field.schema().getType(), this::validateDefaultOther)
                            .apply(field, input));
        };
    }

    private Stream<ValidationException> validateDefaultEnum(Schema.Field field, Schema schema) {
        return !field.schema().getEnumSymbols().contains(UNKNOWN)
                    || (field.defaultVal() != null
                    && field.defaultVal().toString().equals(UNKNOWN)) ? valid() : raise(message(
                            "Default of field " + field.name() + " is \"" + field.defaultVal()
                            + "\". Any Avro enum type that has an \"UNKNOWN\" symbol must set its"
                            + " default value to \"UNKNOWN\".").apply(schema));
    }

    private Stream<ValidationException> validateDefaultUnion(Schema.Field field, Schema schema) {
        return !field.schema().getTypes().contains(Schema.create(Type.NULL))
                || (field.defaultVal() != null
                && field.defaultVal().equals(JsonProperties.NULL_VALUE)) ? valid() : raise(message(
                    "Default of field " + field.name() + " is not null. Any nullable Avro"
                        + " field must specify have its default value set to null.").apply(schema));
    }

    private Stream<ValidationException> validateDefaultOther(Schema.Field field, Schema schema) {
        return field.defaultVal() == null ? valid() : raise(message("Default of field '"
                + field.name() + "' with type " + field.schema().getType() + " is set to "
                + field.defaultVal() + ". The only acceptable default values are the \"UNKNOWN\""
                + "enum symbol and null.").apply(schema));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTime() {
        return validateNonNull(s -> s.getField(TIME),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTimeCompleted() {
        return validateNonNull(s -> s.getField(TIME_COMPLETED),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            message(NOT_TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateTimeReceived() {
        return validateNonNull(s -> s.getField(TIME_RECEIVED),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_RECEIVED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    @Override
    public Validator<Schema> validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
                message(NOT_TIME_RECEIVED_FIELD));
    }

    private static Function<Schema, String> message(String text) {
        return schema -> "Schema " + schema.getFullName() + " is invalid. " + text;
    }
}
