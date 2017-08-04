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
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.Source;
import org.radarcns.specifications.util.Utils;

/**
 * TODO.
 */
public class MonitorSource extends Source {

    private final MonitorSourceType name;

    private final String appProvider;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final String topic;

    private final String key;

    private final String value;

    private final String aggregator;

    /**
     * TODO.
     *
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
    public MonitorSource(
            @JsonProperty("name") MonitorSourceType name,
            @JsonProperty("app_provider") String appProvider,
            @JsonProperty("doc") String doc,
            @JsonProperty("sample_rate") double sampleRate,
            @JsonProperty("unit") Unit unit,
            @JsonProperty("data_type") DataType dataType,
            @JsonProperty("topic") String topic,
            @JsonProperty("key") String key,
            @JsonProperty("value") String value,
            @JsonProperty("aggregator") String aggregator) {
        super(name.name(), doc);
        this.name = name;
        this.appProvider = appProvider;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.topic = topic;
        this.key = key;
        this.value = value;
        this.aggregator = aggregator;
    }

    public MonitorSourceType getType() {
        return name;
    }

    public String getAppProvider() {
        return Utils.getProjectGroup().concat(appProvider);
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
        return Objects.isNull(aggregator) ? null : Utils.getProjectGroup().concat(aggregator);
    }

    @Override
    public Set<String> getTopics() {
        return Collections.singleton(topic);
    }
}
