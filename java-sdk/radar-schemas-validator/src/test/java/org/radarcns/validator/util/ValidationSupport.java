package org.radarcns.validator.util;

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

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;

/**
 * TODO.
 */
public final class ValidationSupport {

    static final ValidationResult VALID = new ValidationResult() {
        public boolean isValid() {
            return true;
        }

        public Optional<String> getReason() {
            return Optional.empty();
        }
    };


    private ValidationSupport() {
        //Static class
    }

    static ValidationResult getValid() {
        return VALID;
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static String getRecordName(Path path) {
        Objects.requireNonNull(path);

        String recordName = "";

        String fileName = path.getFileName().toString();

        boolean start = true;
        for (int i = 0; i < fileName.length(); i++) {
            switch (fileName.charAt(i)) {
                case '_' :
                    start = true;
                    break;
                case '.' : return recordName;
                default:
                    recordName += start ? Character.toUpperCase(fileName.charAt(i))
                        : fileName.charAt(i);
                    start = false;
                    break;
            }
        }

        return recordName;
    }

    /**
     * TODO.
     * @param root TODO
     * @return TODO
     */
    public static List<String> extractEnumerationFields(Schema root) {
        if (root.getType().equals(Type.ENUM)) {
            return root.getEnumSymbols();
        }

        final List<String> symbols = new LinkedList<>();
        root.getFields().stream()
            .filter(field -> field.schema().getType().equals(Type.ENUM))
            .map(field -> field.schema())
            .forEach(schema -> symbols.addAll(schema.getEnumSymbols()));

        return symbols;
    }

}
