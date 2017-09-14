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
import org.radarcns.catalogue.SensorName;
import org.radarcns.schema.specification.Source;
import org.radarcns.schema.specification.Labels;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO.
 */
public class PassiveSource extends Source {
    public enum RadarSourceTypes {
        EMPATICA_E4, PEBBLE_2, ANDROID_PHONE, BIOVOTION_VSM1
    }

    private final String type;

    private final String vendor;

    private final String model;

    private final String appProvider;

    private final Set<Sensor> sensors;

    private final Set<Processor> processors;

    private final Set<String> topics;

    /**
     * TODO.
     * @param vendor TODO
     * @param model TODO
     * @param appProvider TODO
     * @param sensors TODO
     * @param processors TODO
     */
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public PassiveSource(
            @JsonProperty(Labels.VENDOR) String vendor,
            @JsonProperty(Labels.MODEL) String model,
            @JsonProperty(Labels.DOC) String description,
            @JsonProperty(Labels.APP_PROVIDER) String appProvider,
            @JsonProperty(Labels.SENSORS) Set<Sensor> sensors,
            @JsonProperty(Labels.PROCESSORS) Set<Processor> processors) {
        super(vendor + '_' + model, description);

        Objects.requireNonNull(sensors);
        this.type = vendor + '_' + model;
        this.vendor = vendor;
        this.model = model;
        this.appProvider = deduceProjectClass(appProvider);
        this.sensors = sensors;
        this.processors = processors == null ? new HashSet<>() : processors;

        topics = new HashSet<>();

        topics.addAll(this.sensors.stream()
                .flatMap(sensor -> sensor.getTopic().getTopicNames().stream())
                .collect(Collectors.toList()));
        topics.addAll(this.processors.stream()
                .flatMap(proc -> proc.getTopic().getTopicNames().stream())
                .collect(Collectors.toList()));
    }

    public String getType() {
        return type;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public Set<Sensor> getSensors() {
        return sensors;
    }

    /**
     * TODO.
     * @param name TODO
     * @return TODO
     */
    public Sensor getSensor(SensorName name) {
        for (Sensor sensor : sensors) {
            if (sensor.getName().name().equals(name.name())) {
                return sensor;
            }
        }

        throw new IllegalArgumentException(name.name() + " is not a valid sensor for " + getName());
    }

    public Set<Processor> getProcessors() {
        return processors;
    }

    /**
     * TODO.
     * @param name TODO
     * @return TODO
     */
    public Processor getProcessor(SensorName name) {
        for (Processor processor : processors) {
            if (processor.getName().equals(name.name())) {
                return processor;
            }
        }

        throw new IllegalArgumentException(name.name() + " is not a valid sensor for " + getName());
    }

    @Override
    public Set<String> getTopics() {
        return topics;
    }
}
