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
public class Sensor {

    private final SensorName name;

    private final String appProvider;

    private final String doc;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final String topic;

    private final String key;

    private final String value;

    private final String aggregator;

    /**
     * TODO.
     * @param name TODO
     * @param appProvider TODO
     * @param doc TODO
     * @param sampleRate TODO
     * @param unit TODO
     * @param dataType TODO
     * @param topic TODO
     * @param key TODO
     * @param value TODO
     * @param aggregator TODO
     */
    @JsonCreator
    public Sensor(
            @JsonProperty("name") SensorName name,
            @JsonProperty("app_provider") String appProvider,
            @JsonProperty("doc") String doc,
            @JsonProperty("sample_rate") double sampleRate,
            @JsonProperty("unit") Unit unit,
            @JsonProperty("data_type") DataType dataType,
            @JsonProperty("topic") String topic,
            @JsonProperty("key") String key,
            @JsonProperty("value") String value,
            @JsonProperty("aggregator") String aggregator) {
        this.name = name;
        this.appProvider = appProvider;
        this.doc = doc;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.aggregator = aggregator;
    }

    public SensorName getName() {
        return name;
    }

    public String getAppProvider() {
        return Utils.getProjectGroup().concat(appProvider);
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

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return Utils.getProjectGroup().concat(key);
    }

    public String getValue() {
        return Utils.getProjectGroup().concat(value);
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
        set.add(topic);

        if (Utils.isTimedAggregator(aggregator)) {
            set.addAll(TopicUtils.getTimedOutputStateStoreTopics(topic));
        } else {
            set.add(TopicUtils.getOutTopic(topic));
        }

        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Sensor)) {
            return false;
        }

        Sensor sensor = (Sensor) o;

        return new EqualsBuilder()
            .append(sampleRate, sensor.sampleRate)
            .append(name, sensor.name)
            .append(appProvider, sensor.appProvider)
            .append(doc, sensor.doc)
            .append(unit, sensor.unit)
            .append(dataType, sensor.dataType)
            .append(topic, sensor.topic)
            .append(key, sensor.key)
            .append(value, sensor.value)
            .append(aggregator, sensor.aggregator)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .append(appProvider)
            .append(doc)
            .append(sampleRate)
            .append(unit)
            .append(dataType)
            .append(topic)
            .append(key)
            .append(value)
            .append(aggregator)
            .toHashCode();
    }
}
