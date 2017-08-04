package org.radarcns.specifications.source.active.questionnaire;

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
import java.util.List;
import org.radarcns.active.questionnaire.QuestionnaireType;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.specifications.source.active.ActiveSource;

/**
 * TODO.
 */
public class QuestionnaireSource extends ActiveSource {

    private QuestionnaireType name;

    private ActiveSourceType assessmentType;

    private List<Question> questions;

    /**
     * TODO.
     * @param topicName TODO
     * @param keyClass TODO
     * @param valueClass TODO
     * @param description TODO
     * @param name TODO
     * @param assessmentType TODO
     * @param questions TODO
     */
    @JsonCreator
    public QuestionnaireSource(
            @JsonProperty("name") QuestionnaireType name,
            @JsonProperty("assessment_type") ActiveSourceType assessmentType,
            @JsonProperty("description") String description,
            @JsonProperty("topic_name") String topicName,
            @JsonProperty("key_class") String keyClass,
            @JsonProperty("value_class") String valueClass,
            @JsonProperty("questions") List<Question> questions) {
        super(name.name(), topicName, keyClass, valueClass, description);
        this.name = name;
        this.assessmentType = assessmentType;
        this.questions = questions;
    }

    public QuestionnaireType getQuestionnaireType() {
        return name;
    }

    public ActiveSourceType getAssessmentType() {
        return assessmentType;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
