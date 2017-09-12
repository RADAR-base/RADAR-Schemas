package org.radarcns.schema.validation;

import static org.radarcns.schema.validation.SchemaValidationRoles.Message.DEFAULT_VALUE;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.DOC;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.ENUMERATION_SYMBOL;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.ENUMERATION_UNKNOWN_SYMBOL;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.FIELDS;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.FIELD_NAME;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.NOT_TIME_COMPLETED_FIELD;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.NOT_TIME_RECEIVED_FIELD;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.RECORD_NAME;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.SYMBOLS;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.TIME_COMPLETED_FIELD;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.TIME_FIELD;
import static org.radarcns.schema.validation.SchemaValidationRoles.Message.TIME_RECEIVED_FIELD;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.schema.Scope;


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

/**
 * TODO.
 */
@SuppressWarnings("PMD.GodClass")
//TODO split in record and enumerator.
interface SchemaValidationRoles extends Function<Schema, ValidationResult> {

    String TIME = "time";
    String TIME_RECEIVED = "timeReceived";
    String TIME_COMPLETED = "timeCompleted";

    String UNKNOWN = "UNKNOWN";

    Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z.]*$");

    Pattern RECORD_NAME_PATTERN = Pattern.compile("(^[A-Z][a-z]+)|(^[A-Z][a-z0-9]+[A-Z]$)"
                + "|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$)|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+[A-Z]$)");

    Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z]*$");

    Pattern ENUM_SYMBOL_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");

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

    /** Messages. */
    enum Message {
        NAME_SPACE("Namespace cannot be null and must fully lowercase dot separated without "
                + "numeric. In this case the expected value is \""),
        RECORD_NAME("Record name must be the conversion of the .avsc file name in UpperCamelCase "
                + "and must explicitly contain the device name. The expected value is "),
        TIME_FIELD("Any schema representing collected data must have a \"" + TIME
                + "\" field formatted in " + Type.DOUBLE + "."),
        TIME_COMPLETED_FIELD("Any " + Scope.ACTIVE + " schema must have a \""
                + TIME_COMPLETED + "\" field formatted in "
                + Type.DOUBLE + "."),
        NOT_TIME_COMPLETED_FIELD("\"" + TIME_COMPLETED + "\" is allow only in "
                + Scope.ACTIVE + " schemas."),
        TIME_RECEIVED_FIELD("Any " + Scope.PASSIVE
                + " schema must have a \"" + TIME_RECEIVED + "\" field formatted in "
                + Type.DOUBLE + "."),
        NOT_TIME_RECEIVED_FIELD("\"" + TIME_RECEIVED + "\" is allow only in "
                + Scope.PASSIVE + " schemas."),
        FIELDS("Avro Record must have field list."),
        FIELD_NAME("Field name does not respect lowerCamelCase name convention. It cannot contain"
            + " any of the following values ["
            + Stream.of(FieldNameNotAllowed.values())
                  .map(FieldNameNotAllowed::getName)
                  .collect(Collectors.joining(","))
            + "]. Please avoid abbreviations and write out the field name instead."),
        DOC("Documentation is mandatory for any schema and field. The documentation should report "
            + "what is being measured, how, and what units or ranges are applicable. Abbreviations "
            + "and acronyms in the documentation should be written out. The sentence must be ended "
            + "by a point. Please add \"doc\" property."),
        SYMBOLS("Avro Enumerator must have symbol list."),
        ENUMERATION_SYMBOL("Enumerator items should be written in uppercase characters separated "
            + "by underscores."),
        ENUMERATION_UNKNOWN_SYMBOL("Enumerator must contain the \"" + UNKNOWN + "\" symbol. It is "
            + "useful to specify default value for a field using type equals to \"enum\"."),
        DEFAULT_VALUE("Any NULLABLE Avro field must specify a default value. The allowed default "
            + "values are: \"UNKNOWN\" for ENUMERATION, \"MIN_VALUE\" or \"MAX_VALUE\" "
            + "for nullable int and long, \"NaN\" for nullable float and double, \"true\" or "
            + "\"false\" for nullable boolean, \"byte[]\" or \"null\" for bytes, and \"null\" "
            + "for all the other cases.");

        private final String message;

        Message(String message) {
            this.message = message;
        }

        public String getMessage(Schema schema) {
            return message.concat(" ").concat(schema.getFullName()).concat(" is invalid.");
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * TODO.
     * @param scope TODO
     * @return TODO
     */
    static SchemaValidationRoles validateNameSpace(Path schemaPath, Scope scope) {
        String expected = ValidationSupport.getNamespace(schemaPath, scope);

        return schema -> Objects.nonNull(schema.getNamespace())
                                && NAMESPACE_PATTERN.matcher(schema.getNamespace()).matches()
                                && schema.getNamespace().equalsIgnoreCase(expected) ? ValidationResult.valid() :
                                ValidationResult.invalid(Message.NAME_SPACE.getMessage().concat(expected).concat(
                                    "\". ").concat(schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    static SchemaValidationRoles validateRecordName(Path path) {
        return validateRecordName(path, false);
    }

    /**
     * TODO.
     * @param path TODO
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidationRoles validateRecordName(Path path, boolean skip) {
        String expected = ValidationSupport.getRecordName(path);

        return schema ->
                skip || matches(schema.getName(), RECORD_NAME_PATTERN)
                    && schema.getName().equalsIgnoreCase(expected) ? ValidationResult.valid() :
                ValidationResult.invalid(RECORD_NAME.getMessage().concat(expected).concat("\". ").concat(
                    schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateSchemaDocumentation() {
        return validate(schema -> Objects.nonNull(schema.getDoc()), DOC);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateFields() {
        return validate(schema -> !schema.getFields().isEmpty(), FIELDS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateFieldName() {
        return validateFieldName(null);
    }

    /**
     * TODO.
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidationRoles validateFieldName(Set<String> skip) {
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
                },
                FIELD_NAME);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateFieldDocumentation() {
        return validate(schema ->
                schema.getFields()
                    .stream()
                    .allMatch(field -> Objects.nonNull(field.doc())
                        && field.doc().lastIndexOf(".") == field.doc().length() - 1) ,
            DOC);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateSymbols() {
        return validate(schema -> !schema.getEnumSymbols().isEmpty(), SYMBOLS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateEnumerationSymbols() {
        return validate(schema ->
                ValidationSupport.extractEnumerationFields(schema).stream()
                    .allMatch(matches(ENUM_SYMBOL_PATTERN)),
            ENUMERATION_SYMBOL);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateDefault() {
        return validate(ValidationSupport::validateDefault, DEFAULT_VALUE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateTime() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME))
            && schema.getField(TIME).schema().getType().equals(Type.DOUBLE), TIME_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateTimeCompleted() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_COMPLETED))
                && schema.getField(TIME_COMPLETED).schema().getType().equals(Type.DOUBLE),
            TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            NOT_TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateTimeReceived() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_RECEIVED))
                && schema.getField(TIME_RECEIVED).schema().getType().equals(Type.DOUBLE),
            TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
            NOT_TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidationRoles validateUnknownSymbol() {
        return validate(schema -> ValidationSupport.extractEnumerationFields(schema).contains(UNKNOWN),
          ENUMERATION_UNKNOWN_SYMBOL);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static SchemaValidationRoles validate(Predicate<Schema> predicate, Message message) {
        return schema -> predicate.test(schema) ? ValidationResult.valid() : ValidationResult.invalid(message.getMessage(schema));
    }

    /**
     * TODO.
     * @param also TODO
     * @return TODO
     */
    default SchemaValidationRoles and(SchemaValidationRoles also) {
        return schema -> {
            ValidationResult result = this.apply(schema);
            return result.isValid() ? also.apply(schema) : result;
        };
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @return TODO
     */
    static SchemaValidationRoles getGeneralRecordValidator(Path pathToSchema,
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
    static SchemaValidationRoles getGeneralRecordValidator(Path pathToSchema,
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
     * @return TODO
     */
    static SchemaValidationRoles getActiveValidator(Path pathToSchema, Scope root) {
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
    static SchemaValidationRoles getActiveValidator(Path pathToSchema, Scope root,
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
    static SchemaValidationRoles getMonitorValidator(Path pathToSchema, Scope root) {
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
    static SchemaValidationRoles getMonitorValidator(Path pathToSchema, Scope root,
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
    static SchemaValidationRoles getPassiveValidator(Path pathToSchema, Scope root) {
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
    static SchemaValidationRoles getPassiveValidator(Path pathToSchema, Scope root,
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
    static SchemaValidationRoles getGeneralEnumValidator(Path pathToSchema, Scope root) {
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
    static SchemaValidationRoles getGeneralEnumValidator(Path pathToSchema, Scope root,
            boolean skipRecordName) {
        return validateNameSpace(pathToSchema, root)
            .and(validateRecordName(pathToSchema, skipRecordName))
            .and(validateSchemaDocumentation())
            .and(validateSymbols())
            .and(validateEnumerationSymbols())
            .and(validateUnknownSymbol());

    }

    static boolean matches(String str, Pattern pattern) {
        return pattern.matcher(str).matches();
    }

    static Predicate<String> matches(Pattern pattern) {
        return str -> pattern.matcher(str).matches();
    }
}
