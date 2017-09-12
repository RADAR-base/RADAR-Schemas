package org.radarcns.schema.specification.validator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.radarcns.schema.specification.source.active.ActiveSource;
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
interface ActiveSourceRoles extends GenericRoles<ActiveSource> {

    /** Messages. */
    enum ActiveSourceInfo implements Message {
        ASSESSMENT_TYPE("Assessment Type should be equal to ".concat(
                ActiveSource.RadarSourceTypes.QUESTIONNAIRE.name()).concat(".")),
        TOPICS("Topic set is invalid. It should contain only to the topic specified in ".concat(
                "the configuration file."));

        private final String message;

        ActiveSourceInfo(String message) {
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
            && questionnaire.getAssessmentType().equals(
                    ActiveSource.RadarSourceTypes.QUESTIONNAIRE.name())
            ? ValidationResult.valid() : ValidationResult.invalid(ActiveSourceInfo.ASSESSMENT_TYPE.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<QuestionnaireSource> validateTopics() {
        return questionnaire -> {
            Set<String> input = questionnaire.getTopics();
            return Objects.nonNull(input) && input.size() == 1 ?
                ValidationResult.valid() : ValidationResult.invalid(ActiveSourceInfo.TOPICS.getMessage(
                    input == null ? "" : input.stream().map(Object::toString).collect(
                        Collectors.joining(","))));
        };
    }
}
