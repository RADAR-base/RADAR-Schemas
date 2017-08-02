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
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.util.Source;

/**
 * TODO.
 */
public class MonitorSource extends Source {

    private final MonitorSourceType name;

    private final String appProvider;

    private final String doc;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final String topicName;

    private final String keyClass;

    private final String valueClass;

    private final String aggregatorClass;

    /**
     * TODO.
     *
     * @param name TODO
     * @param appProvider TODO
     * @param doc TODO
     * @param sampleRate TODO
     * @param unit TODO
     * @param dataType TODO
     * @param topicName TODO
     * @param keyClass TODO
     * @param valueClass TODO
     * @param aggregatorClass TODO
     */
    @JsonCreator
    public MonitorSource(
            @JsonProperty("name") MonitorSourceType name,
            @JsonProperty("app_provider") String appProvider,
            @JsonProperty("doc") String doc,
            @JsonProperty("sample_rate") double sampleRate,
            @JsonProperty("unit") Unit unit,
            @JsonProperty("data_type") DataType dataType,
            @JsonProperty("topic_name") String topicName,
            @JsonProperty("key_class") String keyClass,
            @JsonProperty("value_class") String valueClass,
            @JsonProperty("aggregator_class") String aggregatorClass) {
        super(name.name());
        this.name = name;
        this.appProvider = appProvider;
        this.doc = doc;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.topicName = topicName;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.aggregatorClass = aggregatorClass;
    }

    public MonitorSourceType getType() {
        return name;
    }

    public String getAppProvider() {
        return appProvider;
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

    public String getTopicName() {
        return topicName;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public String getValueClass() {
        return valueClass;
    }

    public String getAggregatorClass() {
        return aggregatorClass;
    }
}
