package org.radarcns.validator.util;

import static org.radarcns.validator.util.SchemaValidator.Message.NAME_CONVENTION;
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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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

    String FIELD_NAME_REGEX = "^[a-z][a-zA-Z]*$";

    /** Folder names. */
    enum Message {
        NAME_SPACE("Namespace must be in the form \""),
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
        NAME_CONVENTION("Field name does not respect lowerCamelCase name convention.");

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

        return schema -> schema.getNamespace().equalsIgnoreCase(expected) ? valid() :
                                invalid(Message.NAME_SPACE.getMessage().concat(expected).concat(
                                    "\". ").concat(schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @param fileName TODO
     * @return TODO
     */
    static SchemaValidator validateRecordName(String fileName) {
        String expected = getRecordName(fileName);

        return schema -> schema.getName().equalsIgnoreCase(expected) ? valid() :
                invalid(RECORD_NAME.getMessage().concat(expected).concat("\". ").concat(
                    schema.getFullName()).concat(" is invalid."));
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTime() {
        return validate(schema -> schema.getField(TIME) != null
            && schema.getField(TIME).schema().getType().equals(Type.DOUBLE), TIME_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTimeCompleted() {
        return validate(schema -> schema.getField(TIME_COMPLETED) != null
                && schema.getField(TIME_COMPLETED).schema().getType().equals(Type.DOUBLE),
            TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateNotTimeCompleted() {
        return validate(schema -> schema.getField(TIME_COMPLETED) == null,
            NOT_TIME_COMPLETED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateTimeReceived() {
        return validate(schema -> schema.getField(TIME_RECEIVED) != null
                && schema.getField(TIME_RECEIVED).schema().getType().equals(Type.DOUBLE),
            TIME_RECEIVED_FIELD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static SchemaValidator validateNotTimeReceived() {
        return validate(schema -> schema.getField(TIME_RECEIVED) == null,
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
                .allMatch(name -> name.matches(FIELD_NAME_REGEX)
                        || skip != null && skip.contains(name)), NAME_CONVENTION);
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
