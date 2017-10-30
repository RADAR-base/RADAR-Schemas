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

package org.radarcns.schema.specification.active.questionnaire;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.catalogue.RadarWidget;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * TODO.
 */
public class Question {

    @JsonProperty @NotBlank
    private String lead;

    @JsonProperty @NotBlank
    private String content;

    @JsonProperty
    private RadarWidget widget;

    @JsonProperty
    private List<Response> responses;

    public String getLead() {
        return lead;
    }

    public String getContent() {
        return content;
    }

    public RadarWidget getWidget() {
        return widget;
    }

    public List<Response> getResponses() {
        return responses;
    }
}
