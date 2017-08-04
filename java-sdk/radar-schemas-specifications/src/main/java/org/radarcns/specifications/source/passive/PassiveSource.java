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
import org.radarcns.catalogue.PassiveSourceType;
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
            @JsonProperty("vendor") String vendor,
            @JsonProperty("model") String model,
            @JsonProperty("doc") String description,
            @JsonProperty("app_provider") String appProvider,
            @JsonProperty("sensors") Set<Sensor> sensors,
            @JsonProperty("processors") Set<Processor> processors) {
        super(vendor.concat("_").concat(model), description);
        this.type = PassiveSourceType.valueOf(vendor.concat("_").concat(model));
        this.vendor = vendor;
        this.model = model;
        this.appProvider = appProvider;
        this.sensors = sensors;
        this.processors = processors;
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
        return Utils.getProjectGroup().concat(appProvider);
    }

    public Set<Sensor> getSensors() {
        return sensors == null ? new HashSet<>() : sensors;
    }

    public Set<Processor> getProcessors() {
        return processors == null ? new HashSet<>() : processors;
    }

    @Override
    public Set<String> getTopics() {
        Set<String> set = new HashSet<>();

        if (sensors != null && !sensors.isEmpty()) {
            sensors.forEach(sensor -> set.addAll(sensor.getTopics()));
        }

        if (processors != null && !processors.isEmpty()) {
            processors.forEach(proc -> set.addAll(proc.getTopics()));
        }

        return set;
    }
}
