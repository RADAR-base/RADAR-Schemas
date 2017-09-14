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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.specification.Source;
import org.radarcns.schema.specification.Topic;

import java.util.Objects;
import java.util.Set;

import static org.radarcns.schema.specification.Labels.ASSESSMENT_TYPE;
import static org.radarcns.schema.specification.Labels.DOC;
import static org.radarcns.schema.specification.Labels.KEY;
import static org.radarcns.schema.specification.Labels.NAME;
import static org.radarcns.schema.specification.Labels.TOPIC;
import static org.radarcns.schema.specification.Labels.VALUE;

/**
 * TODO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveSource extends Source {

    public enum RadarSourceTypes {
        QUESTIONNAIRE
    }

    private final String assessmentType;

    private final Topic topic;

    private final Set<String> topics;

    /**
     * TODO.
     * @param name TODO
     * @param topic TODO
     * @param key TODO
     * @param value TODO
     * @param description TODO
     */
    @JsonCreator
    public ActiveSource(
            @JsonProperty(ASSESSMENT_TYPE) String assessmentType,
            @JsonProperty(NAME) String name,
            @JsonProperty(TOPIC) String topic,
            @JsonProperty(KEY) String key,
            @JsonProperty(VALUE) String value,
            @JsonProperty(DOC) String description) {
        super(name, description);

        Objects.requireNonNull(assessmentType);
        Objects.requireNonNull(key);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(value);

        this.assessmentType = assessmentType;

        this.topic = new Topic(topic, key, value, null, null);

        this.topics = this.topic.getTopicNames();
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    public Set<String> getTopics() {
        return topics;
    }
}
