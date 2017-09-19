package org.radarcns.schema.validation.rules;

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

import org.radarcns.schema.specification.active.questionnaire.Question;

import java.util.stream.Stream;

import static org.radarcns.schema.validation.rules.Validator.check;
import static org.radarcns.schema.validation.rules.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public final class QuestionRoles {
    private QuestionRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateContent() {
        return validateNonEmpty(Question::getContent,
                "Question \"content\" property is empty.",
                content -> Stream.concat(
                        check(content.charAt(content.length() - 1) == '.',
                        "Question content should be terminated with a period."),
                        check(Character.isUpperCase(content.charAt(0)),
                                "Question content should start with an uppercase character.")));
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateLead() {
        return validateNonEmpty(Question::getLead, "Question \"lead\" is empty.",
                lead -> Stream.concat(
                        check(lead.charAt(lead.length() - 1) == '?',
                        "Question lead should be ended by a question mark: " + lead),
                        check(Character.isUpperCase(lead.charAt(0)),
                        "Question lead should start with an uppercase character.")));
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateWidget() {
        return validateNonNull(Question::getWidget, "Widget cannot be null.");
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateResponses() {
        return validateNonEmpty(Question::getResponses,
                "Responses list cannot be null or empty.");
    }
}
