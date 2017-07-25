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

import java.util.Objects;
import java.util.Optional;

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
     * @param fileName TODO
     * @return TODO
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public static String getRecordName(String fileName) {
        Objects.requireNonNull(fileName);

        String recordName = "";

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
            }
        }

        return recordName;
    }

}
