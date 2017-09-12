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

import static org.radarcns.specifications.util.Labels.APP_PROVIDER;
import static org.radarcns.specifications.util.Labels.DOC;
import static org.radarcns.specifications.util.Labels.MODEL;
import static org.radarcns.specifications.util.Labels.PROCESSORS;
import static org.radarcns.specifications.util.Labels.SENSORS;
import static org.radarcns.specifications.util.Labels.VENDOR;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.catalogue.SensorName;
import org.radarcns.specifications.source.Source;
import org.radarcns.specifications.util.Utils;

/**
 * TODO.
 */
public class PassiveSource extends Source {

    private final PassiveSourceType type;

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
    public PassiveSource(
            @JsonProperty(VENDOR) String vendor,
            @JsonProperty(MODEL) String model,
            @JsonProperty(DOC) String description,
            @JsonProperty(APP_PROVIDER) String appProvider,
            @JsonProperty(SENSORS) Set<Sensor> sensors,
            @JsonProperty(PROCESSORS) Set<Processor> processors) {
        super(vendor.concat("_").concat(model), description);

        Objects.requireNonNull(sensors, SENSORS.concat(" in ").concat(
                PassiveSource.class.getName()).concat(" cannot be null."));

        try {
            this.type = PassiveSourceType.valueOf(vendor.concat("_").concat(model));
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException(PassiveSourceType.getClassSchema().getName() + " in "
                    + PassiveSource.class.getName() + " cannot be null. The concatenation of "
                    + "\"vendor\" and \"model\" separated by underscore must be equal to one of "
                    + "the following values: "
                    + Arrays.stream(PassiveSourceType.values())
                        .map(PassiveSourceType::name)
                        .collect(Collectors.joining(",")), exc);
        }

        this.vendor = vendor;
        this.model = model;
        this.appProvider = Objects.isNull(appProvider)
                ? null : Utils.getProjectGroup().concat(appProvider);
        this.sensors = sensors;
        this.processors = Objects.isNull(processors) ? new HashSet<>() : processors;

        topics = new HashSet<>();

        if (!this.sensors.isEmpty()) {
            sensors.forEach(sensor -> topics.addAll(sensor.getTopic().getTopicNames()));
        }

        if (!this.processors.isEmpty()) {
            processors.forEach(proc -> topics.addAll(proc.getTopic().getTopicNames()));
        }
    }

    public PassiveSourceType getType() {
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
