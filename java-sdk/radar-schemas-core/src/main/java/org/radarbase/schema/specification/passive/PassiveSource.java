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

package org.radarbase.schema.specification.passive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.AppSource;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * TODO.
 */
@JsonInclude(Include.NON_NULL)
public class PassiveSource extends AppSource<PassiveDataTopic> {
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

    @Override
    public String getName() {
        return super.getVendor() + '_' + super.getModel();
    }
}
