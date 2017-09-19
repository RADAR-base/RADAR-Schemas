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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.radarcns.catalogue.RadarWidget;
import org.radarcns.schema.specification.Labels;

/**
 * TODO.
 */
public class Question {

    private final String lead;

    private final String content;

    private final RadarWidget widget;

    private final List<Response> responses;

    /**
     * TODO.
     * @param lead TODO
     * @param content TODO
     * @param widget TODO
     * @param responses TODO
     */
    @JsonCreator
    public Question(
            @JsonProperty(Labels.LEAD) String lead,
            @JsonProperty(Labels.CONTENT) String content,
            @JsonProperty(Labels.WIDGET) RadarWidget widget,
            @JsonProperty(Labels.RESPONSES) List<Response> responses) {

        Objects.requireNonNull(lead);
        Objects.requireNonNull(content);
        Objects.requireNonNull(widget);
        Objects.requireNonNull(responses);

        this.lead = lead;
        this.content = content;
        this.widget = widget;
        this.responses = responses;
    }

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
