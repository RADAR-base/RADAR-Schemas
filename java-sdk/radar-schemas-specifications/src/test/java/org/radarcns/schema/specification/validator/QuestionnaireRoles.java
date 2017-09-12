package org.radarcns.schema.specification.validator;

import static org.radarcns.schema.specification.SourceCatalogue.YAML_EXTENSION;

import java.io.File;
import java.util.Objects;

import org.radarcns.schema.specification.source.active.questionnaire.QuestionnaireSource;

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
interface QuestionnaireRoles extends GenericRoles<QuestionnaireSource> {

    /** Messages. */
    enum QuestionnaireInfo implements Message {
        QUESTIONS("Questions list cannot null or empty."),
        QUESTIONNAIRE_TYPE("Questionnaire Type cannot be null and should match with the ".concat(
                "configuration file name."));

        private final String message;

        QuestionnaireInfo(String message) {
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
     * @param file
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateQuestionnaireType(File file) {
        return questionnaire -> Objects.nonNull(questionnaire.getQuestionnaireType())
                && ValidationSupport.removeExtension(file, YAML_EXTENSION).equalsIgnoreCase(
                        questionnaire.getQuestionnaireType())
                ? ValidationResult.valid() : ValidationResult.invalid(QuestionnaireInfo.QUESTIONNAIRE_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateQuestions() {
        return questionnaire -> Objects.nonNull(questionnaire.getQuestions())
                && !questionnaire.getQuestions().isEmpty()
                ? ValidationResult.valid() : ValidationResult.invalid(QuestionnaireInfo.QUESTIONS.getMessage());
    }
}
