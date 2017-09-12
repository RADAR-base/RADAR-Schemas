package org.radarcns.specifications.source.passive;

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

import static org.radarcns.specifications.util.Labels.AGGREGATOR;
import static org.radarcns.specifications.util.Labels.BASE_OUTPUT_TOPIC;
import static org.radarcns.specifications.util.Labels.PROCESSING_STATE;
import static org.radarcns.specifications.util.Labels.DOC;
import static org.radarcns.specifications.util.Labels.INPUT_KEY;
import static org.radarcns.specifications.util.Labels.INPUT_TOPIC;
import static org.radarcns.specifications.util.Labels.INPUT_VALUE;
import static org.radarcns.specifications.util.Labels.NAME;
import static org.radarcns.specifications.util.Labels.SAMPLE_RATE;
import static org.radarcns.specifications.util.Labels.UNIT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.KafkaActor;
import org.radarcns.specifications.source.Topic;

/**
 * TODO.
 */
public class Processor extends KafkaActor {

    private final String name;

    private static final String NULL_MESSAGE = " in "
            + Processor.class.getName() + " cannot be null.";

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
    public Processor(
            @JsonProperty(NAME) String name,
            @JsonProperty(DOC) String doc,
            @JsonProperty(SAMPLE_RATE) double sampleRate,
            @JsonProperty(UNIT) String unit,
            @JsonProperty(PROCESSING_STATE) DataType dataType,
            @JsonProperty(INPUT_TOPIC) String inputTopic,
            @JsonProperty(INPUT_KEY) String inputKey,
            @JsonProperty(INPUT_VALUE) String inputValue,
            @JsonProperty(BASE_OUTPUT_TOPIC) String baseOutputTopic,
            @JsonProperty(AGGREGATOR) String aggregator) {
        super(doc, sampleRate, unit, dataType,
                new Topic(inputTopic, inputKey, inputValue, aggregator, baseOutputTopic));

        Objects.requireNonNull(baseOutputTopic, BASE_OUTPUT_TOPIC.concat(NULL_MESSAGE));
        Objects.requireNonNull(name, NAME + NULL_MESSAGE);

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
