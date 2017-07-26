package org.radarcns.validator.util;

import static org.radarcns.validator.util.SchemaValidator.Message.DOC;
import static org.radarcns.validator.util.SchemaValidator.Message.FIELDS;
import static org.radarcns.validator.util.SchemaValidator.Message.FILED_NAME;
import static org.radarcns.validator.util.SchemaValidator.Message.NOT_TIME_COMPLETED_FIELD;
import static org.radarcns.validator.util.SchemaValidator.Message.NOT_TIME_RECEIVED_FIELD;
import static org.radarcns.validator.util.SchemaValidator.Message.RECORD_NAME;
import static org.radarcns.validator.util.SchemaValidator.Message.TIME_COMPLETED_FIELD;
import static org.radarcns.validator.util.SchemaValidator.Message.TIME_FIELD;
import static org.radarcns.validator.util.SchemaValidator.Message.TIME_RECEIVED_FIELD;
import static org.radarcns.validator.util.ValidationResult.invalid;
import static org.radarcns.validator.util.ValidationResult.valid;
import static org.radarcns.validator.util.ValidationSupport.getRecordName;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.radarcns.validator.StructureValidator.NameFolder;


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

public interface SchemaValidator extends Function<Schema, ValidationResult> {

    String NAME_SPACE = "org.radarcns";

    String TIME = "time";
    String TIME_RECEIVED = "timeReceived";
    String TIME_COMPLETED = "timeCompleted";

    String NAMESPACE_REGEX = "^[a-z][a-z.]*$";
    String RECORD_NAME_REGEX = "(^[A-Z][a-z]+)|(^[A-Z][a-z0-9]+[A-Z]$)"
                + "|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$)|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+[A-Z]$)";
    String FIELD_NAME_REGEX = "^[a-z][a-zA-Z]*$";

    /** Field names cannot contain the following values. */
    enum FieldNameNotAllowed {
        LOWER_VALUE("value"),
        UPPERD_VALUE("Value");

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
        RECORD_NAME("Record name must be the conversion of the .avsc file name in UpperCamelCase. "
            + "The expected value is "),
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
            + "by a point. Please add \"doc\" property.");

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
     * @param currentFolder TODO
     * @return TODO
     */
    static SchemaValidator validateNameSpace(NameFolder rootFolder, String currentFolder) {
        String expected = NAME_SPACE.concat(".").concat(
                rootFolder.getName()).concat(".").concat(currentFolder);

        return schema -> Objects.nonNull(schema.getNamespace())
                                && schema.getNamespace() .matches(NAMESPACE_REGEX)
                                && schema.getNamespace().equalsIgnoreCase(expected) ? valid() :
                                invalid(Message.NAME_SPACE.getMessage().concat(expected).concat(
                                    "\". ").concat(schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @param fileName TODO
     * @return TODO
     */
    static SchemaValidator validateRecordName(String fileName) {
        return validateRecordName(fileName, null);
    }

    /**
     * TODO.
     * @param fileName TODO
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidator validateRecordName(String fileName, Set<String> skip) {
        String expected = getRecordName(fileName);

        return schema ->
                schema.getName().matches(RECORD_NAME_REGEX)
                    && schema.getName().equalsIgnoreCase(expected)
                    || Objects.nonNull(skip) && skip.contains(schema.getName()) ? valid() :
                invalid(RECORD_NAME.getMessage().concat(expected).concat("\". ").concat(
                    schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateSchemaDocumentation() {
        return validate(schema -> Objects.nonNull(schema.getDoc()), DOC);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateFields() {
        return validate(schema -> !schema.getFields().isEmpty(), FIELDS);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTime() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME))
            && schema.getField(TIME).schema().getType().equals(Type.DOUBLE), TIME_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTimeCompleted() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_COMPLETED))
                && schema.getField(TIME_COMPLETED).schema().getType().equals(Type.DOUBLE),
            TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateNotTimeCompleted() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_COMPLETED)),
            NOT_TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTimeReceived() {
        return validate(schema -> Objects.nonNull(schema.getField(TIME_RECEIVED))
                && schema.getField(TIME_RECEIVED).schema().getType().equals(Type.DOUBLE),
            TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateNotTimeReceived() {
        return validate(schema -> Objects.isNull(schema.getField(TIME_RECEIVED)),
            NOT_TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateFieldName() {
        return validateFieldName(null);
    }

    /**
     * TODO.
     * @param skip TODO
     * @return TODO
     */
    static SchemaValidator validateFieldName(Set<String> skip) {
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
    static SchemaValidator validateFiledDocumentation() {
        return validate(schema ->
            schema.getFields()
                .stream()
                .allMatch(field -> Objects.nonNull(field.doc())
                        && field.doc().lastIndexOf(".") == field.doc().length() - 1) ,
            DOC);
    }

    /**
     * TODO.
     * @param predicate TODO
     * @param message TODO
     * @return TODO
     */
    static SchemaValidator validate(Predicate<Schema> predicate, Message message) {
        return schema -> predicate.test(schema) ? valid() : invalid(message.getMessage(schema));
    }

    /**
     * TODO.
     * @param other TODO
     * @return TODO
     */
    default SchemaValidator and(SchemaValidator other) {
        return schema -> {
            final ValidationResult result = this.apply(schema);
            return result.isValid() ? other.apply(schema) : result;
        };
    }

}
