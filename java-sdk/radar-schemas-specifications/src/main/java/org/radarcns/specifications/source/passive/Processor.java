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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.util.TopicUtils;
import org.radarcns.specifications.util.Utils;

/**
 * TODO.
 */
public class Processor {

    private final SensorName name;

    private final String doc;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final String inputTopic;

    private final String inputKey;

    private final String inputValue;

    private final String baseOutputTopic;

    private final String aggregator;

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
            @JsonProperty("name") SensorName name,
            @JsonProperty("doc") String doc,
            @JsonProperty("sample_rate") double sampleRate,
            @JsonProperty("unit") Unit unit,
            @JsonProperty("data_type") DataType dataType,
            @JsonProperty("input_topic") String inputTopic,
            @JsonProperty("input_key") String inputKey,
            @JsonProperty("input_value") String inputValue,
            @JsonProperty("base_output_topic") String baseOutputTopic,
            @JsonProperty("aggregator") String aggregator) {
        this.name = name;
        this.doc = doc;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.inputTopic = inputTopic;
        this.inputKey = inputKey;
        this.inputValue = inputValue;
        this.baseOutputTopic = baseOutputTopic;
        this.aggregator = aggregator;
    }

    public SensorName getName() {
        return name;
    }

    public String getDoc() {
        return doc;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public Unit getUnit() {
        return unit;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getInputTopic() {
        return inputTopic;
    }

    public String getInputKey() {
        return Utils.getProjectGroup().concat(inputKey);
    }

    public String getInputValue() {
        return Utils.getProjectGroup().concat(inputValue);
    }

    public String getBaseOutputTopic() {
        return baseOutputTopic;
    }

    public String getAggregator() {
        return Utils.getProjectGroup().concat(aggregator);
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getTopics() {
        Set<String> set = new HashSet<>();

        if (Utils.isTimedAggregator(aggregator)) {
            set.addAll(TopicUtils.getTimedOutputStateStoreTopics(baseOutputTopic));
        } else {
            set.add(TopicUtils.getOutTopic(baseOutputTopic));
        }

        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Processor)) {
            return false;
        }

        Processor processor = (Processor) o;

        return new EqualsBuilder()
            .append(sampleRate, processor.sampleRate)
            .append(name, processor.name)
            .append(doc, processor.doc)
            .append(unit, processor.unit)
            .append(dataType, processor.dataType)
            .append(inputTopic, processor.inputTopic)
            .append(inputKey, processor.inputKey)
            .append(inputValue, processor.inputValue)
            .append(baseOutputTopic, processor.baseOutputTopic)
            .append(aggregator, processor.aggregator)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .append(doc)
            .append(sampleRate)
            .append(unit)
            .append(dataType)
            .append(inputTopic)
            .append(inputKey)
            .append(inputValue)
            .append(baseOutputTopic)
            .append(aggregator)
            .toHashCode();
    }
}
