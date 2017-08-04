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

/**
 * TODO.
 */
public class Response {

    private final String text;
    private final Integer score;

    /**
     * TODO.
     * @param text TODO
     * @param score TODO
     */
    @JsonCreator
    public Response(
            @JsonProperty("text") String text,
            @JsonProperty("score") Integer score) {
        this.text = text;
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public Integer getScore() {
        return score;
    }
}
