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

package org.radarcns.schema.specification.passive;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.AppSource;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO.
 */
public class PassiveSource extends AppSource<PassiveDataTopic> {
    public enum RadarSourceTypes {
        EMPATICA_E4, PEBBLE_2, ANDROID_PHONE, BIOVOTION_VSM1
    }

    @JsonProperty @NotEmpty
    private List<PassiveDataTopic> data;

    @Override
    public List<PassiveDataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.PASSIVE;
    }

    public String getName() {
        return super.getVendor() + '_' + super.getModel();
    }

    /**
     * TODO.
     * @param type TODO
     * @return TODO
     */
    public PassiveDataTopic getSensor(@NotNull String type) {
        return data.stream().filter(s -> type.equalsIgnoreCase(s.getType())).findFirst()
                .orElseThrow(() ->  new IllegalArgumentException(
                        type + " is not a valid sensor for " + this.getName()));
    }
}
