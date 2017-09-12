package org.radarcns.schema.specification.source.passive;

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
import org.radarcns.catalogue.SensorName;
import org.radarcns.schema.specification.source.KafkaActor;
import org.radarcns.schema.specification.source.Topic;
import org.radarcns.schema.specification.util.Labels;
import org.radarcns.schema.specification.util.Utils;

import java.util.Objects;

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
            @JsonProperty(Labels.NAME) SensorName name,
            @JsonProperty(Labels.APP_PROVIDER) String appProvider,
            @JsonProperty(Labels.DOC) String doc,
            @JsonProperty(Labels.SAMPLE_RATE) double sampleRate,
            @JsonProperty(Labels.UNIT) String unit,
            @JsonProperty(Labels.PROCESSING_STATE) ProcessingState dataType,
            @JsonProperty(Labels.TOPIC) String topic,
            @JsonProperty(Labels.KEY) String key,
            @JsonProperty(Labels.VALUE) String value,
            @JsonProperty(Labels.AGGREGATOR) String aggregator) {
        super(doc, sampleRate, unit, dataType,
                new Topic(topic, key, value, aggregator, null));

        Objects.requireNonNull(name, Labels.NAME.concat(NULL_MESSAGE));

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
