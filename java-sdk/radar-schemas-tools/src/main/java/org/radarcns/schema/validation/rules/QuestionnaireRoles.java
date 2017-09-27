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

package org.radarcns.schema.validation.rules;

import org.radarcns.schema.specification.active.questionnaire.QuestionnaireDataTopic;

import java.nio.file.Path;

import static org.radarcns.schema.specification.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.schema.validation.ValidationSupport.equalsFileName;
import static org.radarcns.schema.validation.rules.Validator.validateNonEmpty;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public final class QuestionnaireRoles {

    private static final String QUESTIONS = "Questions list cannot null or empty.";
    private static final String QUESTIONNAIRE_TYPE = "Questionnaire Type cannot be null"
            + " and should match with the configuration file name.";

    private QuestionnaireRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<QuestionnaireDataTopic> validateQuestionnaireType(Path file) {
        return validateNonNull(QuestionnaireDataTopic::getType,
                equalsFileName(file, YAML_EXTENSION),
                QUESTIONNAIRE_TYPE);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<QuestionnaireDataTopic> validateQuestions() {
        return validateNonEmpty(QuestionnaireDataTopic::getQuestions, QUESTIONS);
    }
}
