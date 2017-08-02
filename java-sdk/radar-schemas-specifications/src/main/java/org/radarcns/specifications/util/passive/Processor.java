package org.radarcns.specifications.util.passive;

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
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.catalogue.Unit;

/**
 * TODO.
 */
public class Processor {

    private final SensorName name;

    private final String doc;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final String inputTopicName;

    private final String inputKeyClass;

    private final String inputValueClass;

    private final String outputTopicName;

    private final String aggregatorClass;

    /**
     * TODO.
     * @param name TODO
     * @param doc TODO
     * @param sampleRate TODO
     * @param unit TODO
     * @param dataType TODO
     * @param inputTopicName TODO
     * @param inputKeyClass TODO
     * @param inputValueClass TODO
     * @param outputTopicName TODO
     * @param aggregatorClass TODO
     */
    @JsonCreator
    public Processor(
            @JsonProperty("name") SensorName name,
            @JsonProperty("doc") String doc,
            @JsonProperty("sample_rate") double sampleRate,
            @JsonProperty("unit") Unit unit,
            @JsonProperty("data_type") DataType dataType,
            @JsonProperty("input_topic_name") String inputTopicName,
            @JsonProperty("input_key_class") String inputKeyClass,
            @JsonProperty("input_value_class") String inputValueClass,
            @JsonProperty("output_topic_name") String outputTopicName,
            @JsonProperty("aggregator_class") String aggregatorClass) {
        this.name = name;
        this.doc = doc;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.inputTopicName = inputTopicName;
        this.inputKeyClass = inputKeyClass;
        this.inputValueClass = inputValueClass;
        this.outputTopicName = outputTopicName;
        this.aggregatorClass = aggregatorClass;
    }
}
