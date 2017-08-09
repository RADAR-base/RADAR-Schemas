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

import static org.radarcns.specifications.util.Labels.SCORE;
import static org.radarcns.specifications.util.Labels.TEXT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * TODO.
 */
public class Response {

    private final String text;
    private final Integer score;

    private static final String NULL_MESSAGE = " in ".concat(
        Response.class.getName()).concat(" cannot be null.");

    /**
     * TODO.
     * @param text TODO
     * @param score TODO
     */
    @JsonCreator
    public Response(
            @JsonProperty(TEXT) String text,
            @JsonProperty(SCORE) Integer score) {

        Objects.requireNonNull(score, SCORE.concat(NULL_MESSAGE));
        Objects.requireNonNull(text, TEXT.concat(NULL_MESSAGE));

        this.score = score;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Integer getScore() {
        return score;
    }
}
