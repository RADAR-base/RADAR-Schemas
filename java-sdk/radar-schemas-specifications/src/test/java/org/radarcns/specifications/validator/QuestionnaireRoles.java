package org.radarcns.specifications.validator;

import static org.radarcns.specifications.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.specifications.validator.ValidationResult.invalid;
import static org.radarcns.specifications.validator.ValidationResult.valid;
import static org.radarcns.specifications.validator.ValidationSupport.removeExtension;

import java.io.File;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;

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
interface QuestionnaireRoles extends GenericRoles<QuestionnaireSource> {

    /** Messages. */
    enum QuestionnaireInfo implements Message {
        ASSESSMENT_TYPE("Assessment Type should be equal to ".concat(
                ActiveSourceType.QUESTIONNAIRE.name()).concat(".")),
        QUESTIONS("Questions list cannot null or empty."),
        QUESTIONNAIRE_TYPE("Questionnaire Type cannot be null and should match with the "
                + "configuration file name."),
        TOPICS("Topic set is invalid. It should contain only to the topic specified in "
                + "the configuration file.");

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
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateAssessmentType() {
        return questionnaire -> Objects.nonNull(questionnaire.getAssessmentType())
            && questionnaire.getAssessmentType().name().equals(
                    ActiveSourceType.QUESTIONNAIRE.name())
            ? valid() : invalid(QuestionnaireInfo.ASSESSMENT_TYPE.getMessage());
    }

    /**
     * TODO.
     * @param file
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateQuestionnaireType(File file) {
        return questionnaire -> Objects.nonNull(questionnaire.getQuestionnaireType())
                && removeExtension(file, YAML_EXTENSION).equalsIgnoreCase(
                        questionnaire.getQuestionnaireType().name())
                ? valid() : invalid(QuestionnaireInfo.QUESTIONNAIRE_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateQuestions() {
        return questionnaire -> Objects.nonNull(questionnaire.getQuestions())
                && !questionnaire.getQuestions().isEmpty()
                ? valid() : invalid(QuestionnaireInfo.QUESTIONS.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateTopics() {
        return questionnaire -> {
            Set<String> input = questionnaire.getTopics();
            return Objects.nonNull(input) && input.size() == 1
                && input.contains(questionnaire.getTopic()) ?
                valid() : invalid(QuestionnaireInfo.TOPICS.getMessage(
                    input == null ? "" : input.stream().map(Object::toString).collect(
                        Collectors.joining(","))));
        };
    }
}
