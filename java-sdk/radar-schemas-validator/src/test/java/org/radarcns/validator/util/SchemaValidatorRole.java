package org.radarcns.validator.util;

import static org.radarcns.validator.util.SchemaValidatorRole.Message.DEFAULT_VALUE;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.DOC;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.ENUMERATION_SYMBOL;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.ENUMERATION_UNKNOWN_SYMBOL;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.FIELDS;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.FILED_NAME;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.NOT_TIME_COMPLETED_FIELD;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.NOT_TIME_RECEIVED_FIELD;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.RECORD_NAME;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.SYMBOLS;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.TIME_COMPLETED_FIELD;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.TIME_FIELD;
import static org.radarcns.validator.util.SchemaValidatorRole.Message.TIME_RECEIVED_FIELD;
import static org.radarcns.validator.util.ValidationResult.invalid;
import static org.radarcns.validator.util.ValidationResult.valid;
import static org.radarcns.validator.util.ValidationSupport.extractEnumerationFields;
import static org.radarcns.validator.util.ValidationSupport.getNamespace;
import static org.radarcns.validator.util.ValidationSupport.getRecordName;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.validator.SchemaCatalogValidator.NameFolder;


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
interface SchemaValidatorRole extends Function<Schema, ValidationResult> {

    String TIME = "time";
    String TIME_RECEIVED = "timeReceived";
    String TIME_COMPLETED = "timeCompleted";

    String UNKNOWN = "UNKNOWN";

    String NAMESPACE_REGEX = "^[a-z][a-z.]*$";

    String RECORD_NAME_REGEX = "(^[A-Z][a-z]+)|(^[A-Z][a-z0-9]+[A-Z]$)"
                + "|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$)|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+[A-Z]$)";

    String FIELD_NAME_REGEX = "^[a-z][a-zA-Z]*$";

    String ENUMERATION_SYMBOL_REGEX = "^[A-Z0-8_]+$";

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
            + "\" field formatted in " + Type.DOUBLE.getName().toUpperCase(Locale.ENGLISH) + "."),
        TIME_COMPLETED_FIELD("Any " + NameFolder.ACTIVE + " schema must have a \"" + TIME_COMPLETED
            + "\" field formatted in " + Type.DOUBLE.getName().toUpperCase(Locale.ENGLISH) + "."),
        NOT_TIME_COMPLETED_FIELD("\"" + TIME_COMPLETED + "\" is allow only in " + NameFolder.ACTIVE
            + " schemas."),
        TIME_RECEIVED_FIELD("Any " + NameFolder.PASSIVE + " schema must have a \"" + TIME_RECEIVED
            + "\" field formatted in " + Type.DOUBLE.getName().toUpperCase(Locale.ENGLISH) + "."),
        NOT_TIME_RECEIVED_FIELD("\"" + TIME_RECEIVED + "\" is allow only in " + NameFolder.PASSIVE
            + " schemas."),
        FIELDS("Avro Record must have field list."),
        FILED_NAME("Field name does not respect lowerCamelCase name convention. It cannot contain"
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
     * @param rootFolder TODO
     * @param subFolder TODO
     * @return TODO
     */
    static SchemaValidatorRole validateNameSpace(NameFolder rootFolder, String subFolder) {
        String expected = getNamespace(rootFolder, subFolder);

        return schema -> Objects.nonNull(schema.getNamespace())
                                && schema.getNamespace() .matches(NAMESPACE_REGEX)
                                && schema.getNamespace().equalsIgnoreCase(expected) ? valid() :
                                invalid(Message.NAME_SPACE.getMessage().concat(expected).concat(
                                    "\". ").concat(schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    static SchemaValidatorRole validateRecordName(Path path) {
        return validateRecordName(path, false);
    }

    /**
     * TODO.
     * @param path TODO
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidatorRole validateRecordName(Path path, boolean skip) {
        String expected = getRecordName(path);

        return schema ->
                skip || schema.getName().matches(RECORD_NAME_REGEX)
                    && schema.getName().equalsIgnoreCase(expected) ? valid() :
                invalid(RECORD_NAME.getMessage().concat(expected).concat("\". ").concat(
                    schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateSchemaDocumentation() {
        return validate(schema -> Objects.nonNull(schema.getDoc()), DOC);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateFields() {
        return validate(schema -> !schema.getFields().isEmpty(), FIELDS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateFieldName() {
        return validateFieldName(null);
    }

    /**
     * TODO.
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidatorRole validateFieldName(Set<String> skip) {
        return validate(schema ->
                schema.getFields()
                    .stream()
                    .map(field -> field.name())
                    .allMatch(name ->
                        name.matches(FIELD_NAME_REGEX)
                            && Stream.of(FieldNameNotAllowed.values())
                            .noneMatch(notAllowed -> name.contains(notAllowed.getName()))
                            || Objects.nonNull(skip) && skip.contains(name)),
            FILED_NAME);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateFieldDocumentation() {
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
    static SchemaValidatorRole validateSymbols() {
        return validate(schema -> !schema.getEnumSymbols().isEmpty(), SYMBOLS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateEnumerationSymbols() {
        return validate(schema ->
                extractEnumerationFields(schema).stream()
                    .allMatch(symbol -> symbol.matches(
                        ENUMERATION_SYMBOL_REGEX)),
            ENUMERATION_SYMBOL);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateDefault() {
        return validate(schema -> ValidationSupport.validateDefault(schema), DEFAULT_VALUE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateTime() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME))
            && schema.getField(TIME).schema().getType().equals(Type.DOUBLE), TIME_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateTimeCompleted() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_COMPLETED))
                && schema.getField(TIME_COMPLETED).schema().getType().equals(Type.DOUBLE),
            TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            NOT_TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateTimeReceived() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_RECEIVED))
                && schema.getField(TIME_RECEIVED).schema().getType().equals(Type.DOUBLE),
            TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
            NOT_TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidatorRole validateUnknownSymbol() {
        return validate(schema -> extractEnumerationFields(schema).contains(UNKNOWN),
          ENUMERATION_UNKNOWN_SYMBOL);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static SchemaValidatorRole validate(Predicate<Schema> predicate, Message message) {
        return schema -> predicate.test(schema) ? valid() : invalid(message.getMessage(schema));
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default SchemaValidatorRole and(SchemaValidatorRole other) {
        return schema -> {
            final ValidationResult result = this.apply(schema);
            return result.isValid() ? other.apply(schema) : result;
        };
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @return TODO
     */
    static SchemaValidatorRole getGeneralRecordValidator(Path pathToSchema, NameFolder root,
            String subfolder) {
        return validateNameSpace(root, subfolder)
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
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    static SchemaValidatorRole getGeneralRecordValidator(Path pathToSchema, NameFolder root,
            String subfolder, boolean skipRecordName, Set<String> skipFieldName) {
        return validateNameSpace(root, subfolder)
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
     * @param subfolder TODO
     * @return TODO
     */
    static SchemaValidatorRole getActiveValidator(Path pathToSchema, NameFolder root,
            String subfolder) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder)
                  .and(validateTime())
                  .and(validateTimeCompleted())
                  .and(validateNotTimeReceived());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    static SchemaValidatorRole getActiveValidator(Path pathToSchema, NameFolder root,
            String subfolder,     boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder, skipRecordName,
            skipFieldName)
                  .and(validateTime())
                  .and(validateTimeCompleted())
                  .and(validateNotTimeReceived());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @return TODO
     */
    static SchemaValidatorRole getMonitorValidator(Path pathToSchema, NameFolder root,
            String subfolder) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder)
            .and(validateTime());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    static SchemaValidatorRole getMonitorValidator(Path pathToSchema, NameFolder root,
            String subfolder,     boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder, skipRecordName,
            skipFieldName)
                  .and(validateTime());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @return TODO
     */
    static SchemaValidatorRole getPassiveValidator(Path pathToSchema, NameFolder root,
            String subfolder) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder)
            .and(validateTime())
            .and(validateTimeReceived())
            .and(validateNotTimeCompleted());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     */
    static SchemaValidatorRole getPassiveValidator(Path pathToSchema, NameFolder root,
            String subfolder,     boolean skipRecordName, Set<String> skipFieldName) {
        return getGeneralRecordValidator(pathToSchema, root, subfolder, skipRecordName,
              skipFieldName)
            .and(validateTime())
            .and(validateTimeReceived())
            .and(validateNotTimeCompleted());
    }

    /**
     * TODO.
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @return TODO
     */
    static SchemaValidatorRole getGeneralEnumValidator(Path pathToSchema, NameFolder root,
            String subfolder) {
        return validateNameSpace(root, subfolder)
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
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @return TODO
     */
    static SchemaValidatorRole getGeneralEnumValidator(Path pathToSchema, NameFolder root,
            String subfolder,     boolean skipRecordName) {
        return validateNameSpace(root, subfolder)
            .and(validateRecordName(pathToSchema, skipRecordName))
            .and(validateSchemaDocumentation())
            .and(validateSymbols())
            .and(validateEnumerationSymbols())
            .and(validateUnknownSymbol());

    }

}
