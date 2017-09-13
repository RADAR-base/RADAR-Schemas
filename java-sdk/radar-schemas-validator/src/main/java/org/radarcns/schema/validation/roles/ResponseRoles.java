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

import org.radarcns.schema.specification.source.active.questionnaire.Response;

import static org.radarcns.schema.validation.roles.Validator.validateNonNull;

/**
 * TODO.
 */
public final class ResponseRoles {
    private static final String SCORE = "Answer score cannot be null.";
    private static final String TEXT = "Answer text cannot be null.";

    private ResponseRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Response> validateScore() {
        return validateNonNull(Response::getScore, SCORE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Response> validateText() {
        return validateNonNull(Response::getText, TEXT);
    }
}