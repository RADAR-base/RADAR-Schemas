package org.radarcns.specifications.validator;

import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;

import java.util.Objects;
import org.radarcns.specifications.source.active.questionnaire.Response;

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
interface ResponseRoles extends GenericRoles<Response> {

    /** Messages. */
    enum ResponseInfo implements Message {
        SCORE("Answer score cannot be null."),
        TEXT("Answer text cannot be null.");

        private final String message;

        ResponseInfo(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getMessage(String info) {
            return message.concat(" ").concat(info);
        }
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Response> validateScore() {
        return response -> Objects.nonNull(response.getScore())
                ? valid() : invalid(ResponseInfo.SCORE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Response> validateText() {
        return response -> Objects.nonNull(response.getText())
                ? valid() : invalid(ResponseInfo.TEXT.getMessage());
    }
}
