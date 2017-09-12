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
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.KafkaActor;
import org.radarcns.specifications.source.Topic;
import org.radarcns.specifications.util.Utils;

import java.util.Objects;

import static org.radarcns.specifications.util.Labels.AGGREGATOR;
import static org.radarcns.specifications.util.Labels.APP_PROVIDER;
import static org.radarcns.specifications.util.Labels.PROCESSING_STATE;
import static org.radarcns.specifications.util.Labels.DOC;
import static org.radarcns.specifications.util.Labels.KEY;
import static org.radarcns.specifications.util.Labels.NAME;
import static org.radarcns.specifications.util.Labels.SAMPLE_RATE;
import static org.radarcns.specifications.util.Labels.TOPIC;
import static org.radarcns.specifications.util.Labels.UNIT;
import static org.radarcns.specifications.util.Labels.VALUE;

/**
 * TODO.
 */
public class Sensor extends KafkaActor {

    private final SensorName name;
    private final String appProvider;

    private static final String NULL_MESSAGE = " in ".concat(
        Processor.class.getName()).concat(" cannot be null.");

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
            @JsonProperty(NAME) SensorName name,
            @JsonProperty(APP_PROVIDER) String appProvider,
            @JsonProperty(DOC) String doc,
            @JsonProperty(SAMPLE_RATE) double sampleRate,
            @JsonProperty(UNIT) String unit,
            @JsonProperty(PROCESSING_STATE) DataType dataType,
            @JsonProperty(TOPIC) String topic,
            @JsonProperty(KEY) String key,
            @JsonProperty(VALUE) String value,
            @JsonProperty(AGGREGATOR) String aggregator) {
        super(doc, sampleRate, unit, dataType,
                new Topic(topic, key, value, aggregator, null));

        Objects.requireNonNull(name, NAME.concat(NULL_MESSAGE));

        this.name = name;
        this.appProvider = Objects.isNull(appProvider)
                ? null : Utils.getProjectGroup().concat(appProvider);
    }

    public String getAppProvider() {
        return appProvider;
    }

    public SensorName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) return false;
        Sensor sensor = (Sensor) o;
        return name == sensor.name &&
                Objects.equals(appProvider, sensor.appProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, appProvider);
    }
}
