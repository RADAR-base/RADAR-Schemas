package org.radarcns.schema.specification.validator;

import static org.radarcns.schema.specification.validator.ValidationResult.invalid;
import static org.radarcns.schema.specification.validator.ValidationResult.valid;

import java.util.Objects;
import org.radarcns.catalogue.RadarWidget;
import org.radarcns.schema.specification.source.active.questionnaire.Question;

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
interface QuestionRoles extends GenericRoles<Question> {

    /** Messages. */
    enum QuestionInfo implements Message {
        CONTENT("Question content cannot be null and should be ended by a point."),
        LEAD("Question lead cannot be null and should be ended by a question mark."),
        WIDGET("Widget cannot be null and must be different from ".concat(
                RadarWidget.UNKNOWN.name()).concat(".")),
        RESPONSES("Responses list cannot be null or empty.");

        private final String message;

        QuestionInfo(String message) {
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
    static GenericRoles<Question> validateContent() {
        return question -> Objects.nonNull(question.getContent())
                && question.getContent().endsWith(".")
                ? valid() : invalid(QuestionInfo.CONTENT.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Question> validateLead() {
        return question -> Objects.nonNull(question.getLead())
                && question.getLead().endsWith("?")
                ? valid() : invalid(QuestionInfo.LEAD.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Question> validateWidget() {
        return question -> Objects.nonNull(question.getWidget())
                && !question.getWidget().name().equals(RadarWidget.UNKNOWN.name())
                ? valid() : invalid(QuestionInfo.WIDGET.getMessage());
    }

    /**
     * TODO.
     * @return TODO
     */
    static GenericRoles<Question> validateResponses() {
        return question -> Objects.nonNull(question.getResponses())
                && !question.getResponses().isEmpty()
                ? valid() : invalid(QuestionInfo.RESPONSES.getMessage());
    }
}
