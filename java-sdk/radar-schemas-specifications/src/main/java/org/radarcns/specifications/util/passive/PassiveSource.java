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
import java.util.List;
import org.radarcns.specifications.util.Source;

/**
 * TODO.
 */
public class PassiveSource extends Source {

    private final String vendor;

    private final String model;

    private final String appProvider;

    private final List<Sensor> sensors;

    private final List<Processor> processors;

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
            @JsonProperty("app_provider") String appProvider,
            @JsonProperty("sensors") List<Sensor> sensors,
            @JsonProperty("processors") List<Processor> processors) {
        super(vendor.concat("_").concat(model));
        this.vendor = vendor;
        this.model = model;
        this.appProvider = appProvider;
        this.sensors = sensors;
        this.processors = processors;
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

    public List<Sensor> getSensors() {
        return sensors;
    }

    public List<Processor> getProcessors() {
        return processors;
    }
}
