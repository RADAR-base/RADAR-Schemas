package org.radarcns.specifications.util.active;

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
import org.radarcns.specifications.util.Source;

/**
 * TODO.
 */
public abstract class ActiveSource extends Source {

    private final String topicName;

    private final String keyClass;

    private final String valueClass;

    private final String doc;

    /**
     * TODO.
     * @param name TODO
     * @param topicName TODO
     * @param keyClass TODO
     * @param valueClass TODO
     * @param description TODO
     */
    @JsonCreator
    public ActiveSource(
            @JsonProperty("name") String name,
            @JsonProperty("topic_name") String topicName,
            @JsonProperty("key_class") String keyClass,
            @JsonProperty("value_class") String valueClass,
            @JsonProperty("doc") String description) {
        super(name);
        this.topicName = topicName;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.doc = description;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public String getValueClass() {
        return valueClass;
    }

    public String getDoc() {
        return doc;
    }
}
