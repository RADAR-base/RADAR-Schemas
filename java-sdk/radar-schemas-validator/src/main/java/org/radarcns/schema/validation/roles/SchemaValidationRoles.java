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

package org.radarcns.schema.validation.roles;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.ValidationSupport;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.radarcns.schema.validation.ValidationSupport.extractEnumerationFields;
import static org.radarcns.schema.validation.ValidationSupport.nonEmpty;
import static org.radarcns.schema.validation.roles.Validator.matches;
import static org.radarcns.schema.validation.roles.Validator.validate;
import static org.radarcns.schema.validation.roles.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.roles.Validator.validateNonNull;


/**
 * TODO.
 */
@SuppressWarnings("PMD.GodClass")
//TODO split in record and enumerator.
public final class SchemaValidationRoles {

    static final String TIME = "time";
    private static final String TIME_RECEIVED = "timeReceived";
    private static final String TIME_COMPLETED = "timeCompleted";

    public static final String UNKNOWN = "UNKNOWN";

    static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z.]*$");

    static final Pattern RECORD_NAME_PATTERN = Pattern.compile("(^[A-Z][a-z]+)"
            + "|(^[A-Z][a-z0-9]+[A-Z]$)"
            + "|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$)"
            + "|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+[A-Z]$)");

    static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z]*$");

    static final Pattern ENUM_SYMBOL_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

    /** Field names cannot contain the following values. */
    enum FieldNameNotAllowed {
        LOWER_VALUE("value"),
        UPPER_VALUE("Value"),
        LOWER_VAL("val"),
        UPPER_VAL("Val");

        private final String name;

        FieldNameNotAllowed(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String NAME_SPACE = "Namespace cannot be null and must fully lowercase dot"
            + " separated without numeric. In this case the expected value is \"";
    private static final String RECORD_NAME = "Record name must be the conversion of the .avsc file"
            + " name in UpperCamelCase and must explicitly contain the device name."
            + " The expected value is ";
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
    private static final String FIELD_NAME = "Field name does not respect lowerCamelCase name"
            + " convention. It cannot contain any of the following values ["
            + Arrays.stream(FieldNameNotAllowed.values())
                .map(FieldNameNotAllowed::getName)
                .collect(Collectors.joining(", "))
            + "]. Please avoid abbreviations and write out the field name instead.";
    private static final String DOC = "Documentation is mandatory for any schema and field."
            + " The documentation should report "
            + "what is being measured, how, and what units or ranges are applicable. Abbreviations "
            + "and acronyms in the documentation should be written out. The sentence must be ended "
            + "by a point. Please add \"doc\" property.";
    private static final String SYMBOLS = "Avro Enumerator must have symbol list.";
    private static final String ENUMERATION_SYMBOL = "Enumerator items should be written in"
            + " uppercase characters separated by underscores.";
    private static final String ENUMERATION_UNKNOWN_SYMBOL = "Enumerator must contain the \""
            + UNKNOWN + "\" symbol. It is useful to specify default value for a field using type"
            + " equals to \"enum\".";
    private static final String DEFAULT_VALUE = "Any NULLABLE Avro field must specify a default"
            + " value. The allowed default values are: \"UNKNOWN\" for ENUMERATION, and \"null\" "
            + "for all the other cases.";

    private SchemaValidationRoles() {
        // utility class
    }

    /**
     * TODO.
     * @param scope TODO
     * @return TODO
     */
    public static Validator<Schema> validateNameSpace(Path schemaPath, Scope scope) {
        String expected = ValidationSupport.getNamespace(schemaPath, scope);

        return validateNonNull(Schema::getNamespace,
                namespace -> matches(namespace, NAMESPACE_PATTERN)
                        && namespace.equalsIgnoreCase(expected),
                schema -> NAME_SPACE + expected + "\". " + schema.getFullName() + " is invalid.");
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static Validator<Schema> validateRecordName(Path path) {
        return validateRecordName(path, false);
    }

    /**
     * TODO.
     * @param path TODO
     * @param skip TODO
     * @return TODO
     */
    public static Validator<Schema> validateRecordName(Path path, boolean skip) {
        String expected = ValidationSupport.getRecordName(path);

        return validate(schema -> skip
                        || matches(schema.getName(), RECORD_NAME_PATTERN)
                        && schema.getName().equalsIgnoreCase(expected),
                schema -> RECORD_NAME + '"' + expected + "\". " + schema.getFullName()
                        + " is invalid.");
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateSchemaDocumentation() {
        return validateNonNull(Schema::getDoc, doc -> doc.charAt(doc.length() - 1) == '.',
                message(DOC));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateFields() {
        return validateNonEmpty(Schema::getFields, message(FIELDS));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateFieldName() {
        return validateFieldName(null);
    }

    /**
     * TODO.
     * @param skip TODO
     * @return TODO
     */
    public static Validator<Schema> validateFieldName(Set<String> skip) {
        return validate(schema -> {
            Stream<String> stream = schema.getFields()
                    .stream()
                    .map(Schema.Field::name);
            if (skip != null && !skip.isEmpty()) {
                stream = stream.filter(name -> !skip.contains(name));
            }
            return stream.allMatch(name ->
                    matches(name, FIELD_NAME_PATTERN)
                            && Stream.of(FieldNameNotAllowed.values())
                            .noneMatch(notAllowed -> name.contains(notAllowed.getName())));
        }, message(FIELD_NAME));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateFieldDocumentation() {
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
    public static Validator<Schema> validateSymbols() {
        return validate(schema -> nonEmpty(schema.getEnumSymbols()), message(SYMBOLS));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateEnumerationSymbols() {
        return validate(schema ->
                extractEnumerationFields(schema).stream().allMatch(matches(ENUM_SYMBOL_PATTERN)),
            message(ENUMERATION_SYMBOL));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateDefault() {
        return validate(ValidationSupport::validateDefault, message(DEFAULT_VALUE));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateTime() {
        return validateNonNull(s -> s.getField(TIME),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateTimeCompleted() {
        return validateNonNull(s -> s.getField(TIME_COMPLETED),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            message(NOT_TIME_COMPLETED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateTimeReceived() {
        return validateNonNull(s -> s.getField(TIME_RECEIVED),
                time -> time.schema().getType().equals(Type.DOUBLE), message(TIME_RECEIVED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
                message(NOT_TIME_RECEIVED_FIELD));
    }

    /**
     * TODO.
     * @return TODO
     */
    public static Validator<Schema> validateUnknownSymbol() {
        return validate(schema -> extractEnumerationFields(schema).contains(UNKNOWN),
                message(ENUMERATION_UNKNOWN_SYMBOL));
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO
     */
    public static Validator<Schema> getGeneralRecordValidator(Path pathToSchema,
            Scope root) {
        return validateNameSpace(pathToSchema, root)
                  .and(validateRecordName(pathToSchema))
                  .and(validateSchemaDocumentation())
                  .and(validateFields())
                  .and(validateFieldName())
                  .and(validateFieldDocumentation())
                  .and(validateEnumerationSymbols());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    public static Validator<Schema> getGeneralRecordValidator(Path pathToSchema,
            Scope root, boolean skipRecordName,
            Set<String> skipFieldName) {
        return validateNameSpace(pathToSchema, root)
                  .and(validateRecordName(pathToSchema, skipRecordName))
                  .and(validateSchemaDocumentation())
                  .and(validateFields())
                  .and(validateFieldName(skipFieldName))
                  .and(validateFieldDocumentation())
                  .and(validateEnumerationSymbols());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO7
     */
    public static Validator<Schema> getActiveValidator(Path pathToSchema, Scope root) {
        return getGeneralRecordValidator(pathToSchema, root)
                  .and(validateTime())
                  .and(validateTimeCompleted())
                  .and(validateNotTimeReceived());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    public static Validator<Schema> getActiveValidator(Path pathToSchema, Scope root,
            boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, skipRecordName,
            skipFieldName)
                  .and(validateTime())
                  .and(validateTimeCompleted())
                  .and(validateNotTimeReceived());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO
     */
    public static Validator<Schema> getMonitorValidator(Path pathToSchema, Scope root) {
        return getGeneralRecordValidator(pathToSchema, root)
            .and(validateTime());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    public static Validator<Schema> getMonitorValidator(Path pathToSchema, Scope root,
             boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, skipRecordName,
            skipFieldName)
                  .and(validateTime());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO
     */
    public static Validator<Schema> getPassiveValidator(Path pathToSchema, Scope root) {
        return getGeneralRecordValidator(pathToSchema, root)
            .and(validateTime())
            .and(validateTimeReceived())
            .and(validateNotTimeCompleted());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    public static Validator<Schema> getPassiveValidator(Path pathToSchema, Scope root,
            boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, skipRecordName,
              skipFieldName)
            .and(validateTime())
            .and(validateTimeReceived())
            .and(validateNotTimeCompleted());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO
     */
    public static Validator<Schema> getGeneralEnumValidator(Path pathToSchema, Scope root) {
        return validateNameSpace(pathToSchema, root)
            .and(validateRecordName(pathToSchema))
            .and(validateSchemaDocumentation())
            .and(validateSymbols())
            .and(validateEnumerationSymbols())
            .and(validateUnknownSymbol());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param skipRecordName TODO
     * @return TODO
     */
    public static Validator<Schema> getGeneralEnumValidator(Path pathToSchema, Scope root,
            boolean skipRecordName) {
        return validateNameSpace(pathToSchema, root)
            .and(validateRecordName(pathToSchema, skipRecordName))
            .and(validateSchemaDocumentation())
            .and(validateSymbols())
            .and(validateEnumerationSymbols())
            .and(validateUnknownSymbol());
    }

    private static Function<Schema, String> message(String text) {
        return schema -> text + ' ' + schema.getFullName() + " is invalid.";
    }
}
