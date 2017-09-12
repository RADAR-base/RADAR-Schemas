package org.radarcns.schema.specification.source.active.questionnaire;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.specification.source.active.ActiveSource;

import java.util.List;
import java.util.Objects;

import static org.radarcns.schema.specification.util.Labels.ASSESSMENT_TYPE;
import static org.radarcns.schema.specification.util.Labels.DOC;
import static org.radarcns.schema.specification.util.Labels.KEY;
import static org.radarcns.schema.specification.util.Labels.NAME;
import static org.radarcns.schema.specification.util.Labels.QUESTIONS;
import static org.radarcns.schema.specification.util.Labels.TOPIC;
import static org.radarcns.schema.specification.util.Labels.VALUE;

/**
 * TODO.
 */
public class QuestionnaireSource extends ActiveSource {
    public enum RadarSourceTypes {
        PHQ8
    }

    private String type;

    private List<Question> questions;

    /**
     * TODO.
     * @param topic TODO
     * @param key TODO
     * @param value TODO
     * @param doc TODO
     * @param name TODO
     * @param assessmentType TODO
     * @param questions TODO
     */
    @JsonCreator
    public QuestionnaireSource(
            @JsonProperty(ASSESSMENT_TYPE) String assessmentType,
            @JsonProperty(NAME) String name,
            @JsonProperty(DOC) String doc,
            @JsonProperty(TOPIC) String topic,
            @JsonProperty(KEY) String key,
            @JsonProperty(VALUE) String value,
            @JsonProperty(QUESTIONS) List<Question> questions) {
        super(assessmentType, name, topic, key, value, doc);
        this.type = name;

        Objects.requireNonNull(questions, QUESTIONS.concat(" in ").concat(
                QuestionnaireSource.class.getName()).concat(" cannot be null."));
        this.questions = questions;
    }

    public String getQuestionnaireType() {
        return type;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
