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
import java.util.Objects;
import org.radarbase.schema.specification.AppDataTopic;
import org.radarcns.catalogue.ProcessingState;

/**
 * TODO.
 */
@JsonInclude(Include.NON_NULL)
public class PassiveDataTopic extends AppDataTopic {

    @JsonProperty("processing_state")
    private ProcessingState processingState;

    public ProcessingState getProcessingState() {
        return processingState;
    }

    public void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        PassiveDataTopic passiveData = (PassiveDataTopic) o;
        return Objects.equals(processingState, passiveData.processingState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processingState);
    }
}
