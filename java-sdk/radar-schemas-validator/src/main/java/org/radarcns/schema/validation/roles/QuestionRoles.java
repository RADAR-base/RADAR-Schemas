package org.radarcns.schema.validation.roles;

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

import org.radarcns.catalogue.RadarWidget;
import org.radarcns.schema.specification.source.active.questionnaire.Question;

import static org.radarcns.schema.validation.roles.Validator.validateNonNull;

/**
 * TODO.
 */
public final class QuestionRoles {
    private static final String CONTENT = "Question content cannot be null and should be ended by"
            + " a point.";
    private static final String LEAD = "Question lead cannot be null and should be ended by a"
            + " question mark.";
    private static final String WIDGET = "Widget cannot be null and must be different from "
            + RadarWidget.UNKNOWN.name() + '.';
    private static final String RESPONSES = "Responses list cannot be null or empty.";


    private QuestionRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateContent() {
        return validateNonNull(Question::getContent, content -> content.endsWith("."), CONTENT);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateLead() {
        return validateNonNull(Question::getLead, lead -> lead.endsWith("?"), LEAD);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateWidget() {
        return validateNonNull(Question::getWidget, widget -> widget != RadarWidget.UNKNOWN,
                WIDGET);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<Question> validateResponses() {
        return validateNonNull(Question::getResponses, responses -> !responses.isEmpty(),
                RESPONSES);
    }
}
