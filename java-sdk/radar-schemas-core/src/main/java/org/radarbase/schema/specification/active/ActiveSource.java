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

package org.radarbase.schema.specification.active;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.DataProducer;
import org.radarbase.schema.specification.DataTopic;
import org.radarbase.schema.specification.active.questionnaire.QuestionnaireSource;

/**
 * TODO.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "assessment_type")
@JsonSubTypes(value = {
        @JsonSubTypes.Type(name = "QUESTIONNAIRE", value = QuestionnaireSource.class),
        @JsonSubTypes.Type(name = "APP", value = AppActiveSource.class)})
@JsonInclude(Include.NON_NULL)
public class ActiveSource<T extends DataTopic> extends DataProducer<T> {
    public enum RadarSourceTypes {
        QUESTIONNAIRE
    }

    @JsonProperty("assessment_type")
    @NotBlank
    private String assessmentType;

    @JsonProperty
    private List<T> data;

    @JsonProperty
    private String vendor;

    @JsonProperty
    private String model;

    @JsonProperty
    private String version;

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

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }
}
