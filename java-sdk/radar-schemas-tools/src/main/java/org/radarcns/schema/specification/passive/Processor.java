package org.radarcns.schema.specification.passive;

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
import org.radarcns.catalogue.ProcessingState;
import org.radarcns.schema.specification.KafkaActor;
import org.radarcns.schema.specification.Topic;
import org.radarcns.schema.specification.Labels;

import java.util.Objects;

/**
 * TODO.
 */
public class Processor extends KafkaActor {

    private final String name;

    /**
     * TODO.
     * @param name TODO
     * @param doc TODO
     * @param sampleRate TODO
     * @param unit TODO
     * @param dataType TODO
     * @param inputTopic TODO
     * @param inputKey TODO
     * @param inputValue TODO
     * @param baseOutputTopic TODO
     * @param aggregator TODO
     */
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Processor(
            @JsonProperty(Labels.NAME) String name,
            @JsonProperty(Labels.DOC) String doc,
            @JsonProperty(Labels.DEFAULT_SAMPLE_INTERVAL) double sampleInterval,
            @JsonProperty(Labels.DEFAULT_SAMPLE_RATE) double sampleRate,
            @JsonProperty(Labels.UNIT) String unit,
            @JsonProperty(Labels.PROCESSING_STATE) ProcessingState dataType,
            @JsonProperty(Labels.INPUT_TOPIC) String inputTopic,
            @JsonProperty(Labels.INPUT_KEY) String inputKey,
            @JsonProperty(Labels.INPUT_VALUE) String inputValue,
            @JsonProperty(Labels.BASE_OUTPUT_TOPIC) String baseOutputTopic,
            @JsonProperty(Labels.AGGREGATOR) String aggregator) {
        super(doc, sampleInterval, sampleRate, unit, dataType,
                new Topic(inputTopic, inputKey, inputValue, aggregator, baseOutputTopic));

        Objects.requireNonNull(baseOutputTopic);
        Objects.requireNonNull(name);

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
