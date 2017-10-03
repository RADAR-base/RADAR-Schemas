package org.radarcns.schema.specification.active;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.DataTopic;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.active.questionnaire.QuestionnaireSource;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * TODO.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "assessment_type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = "QUESTIONNAIRE", value = QuestionnaireSource.class),
        @JsonSubTypes.Type(name = "APP", value = AppActiveSource.class)})
public class ActiveSource<T extends DataTopic> extends DataProducer<T> {
    public enum RadarSourceTypes {
        QUESTIONNAIRE
    }

    @JsonProperty("assessment_type") @NotBlank
    private String assessmentType;

    @JsonProperty
    private List<T> data;

    public String getAssessmentType() {
        return assessmentType;
    }

    @Override
    public List<T> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.ACTIVE;
    }
}
