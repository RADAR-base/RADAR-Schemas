package org.radarcns.specifications.source.active;

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

import static org.radarcns.specifications.util.Labels.ASSESSMENT_TYPE;
import static org.radarcns.specifications.util.Labels.DOC;
import static org.radarcns.specifications.util.Labels.KEY;
import static org.radarcns.specifications.util.Labels.NAME;
import static org.radarcns.specifications.util.Labels.TOPIC;
import static org.radarcns.specifications.util.Labels.VALUE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import org.radarcns.catalogue.ActiveSourceType;
import org.radarcns.specifications.source.Source;
import org.radarcns.specifications.source.Topic;

/**
 * TODO.
 */
public abstract class ActiveSource extends Source {

    private ActiveSourceType assessmentType;

    private final Topic topic;

    private final Set<String> topics;

    private static final String NULL_MESSAGE = " in ".concat(
            ActiveSource.class.getName()).concat(" cannot be null.");

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
            @JsonProperty(ASSESSMENT_TYPE) ActiveSourceType assessmentType,
            @JsonProperty(NAME) String name,
            @JsonProperty(TOPIC) String topic,
            @JsonProperty(KEY) String key,
            @JsonProperty(VALUE) String value,
            @JsonProperty(DOC) String description) {
        super(name, description);

        Objects.requireNonNull(assessmentType, ASSESSMENT_TYPE.concat(NULL_MESSAGE));
        Objects.requireNonNull(key, KEY.concat(NULL_MESSAGE));
        Objects.requireNonNull(topic, TOPIC.concat(NULL_MESSAGE));
        Objects.requireNonNull(value, VALUE.concat(NULL_MESSAGE));

        this.assessmentType = assessmentType;

        this.topic = new Topic(topic, key, value, null, null);

        this.topics = this.topic.getTopicNames();
    }

    public ActiveSourceType getAssessmentType() {
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
