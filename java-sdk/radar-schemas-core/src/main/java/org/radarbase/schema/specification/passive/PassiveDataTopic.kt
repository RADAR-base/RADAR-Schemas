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
package org.radarbase.schema.specification.passive

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.schema.specification.AppDataTopic
import org.radarcns.catalogue.ProcessingState
import java.util.Objects

@JsonInclude(NON_NULL)
class PassiveDataTopic : AppDataTopic() {
    @JsonProperty("processing_state")
    var processingState: ProcessingState? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (!super.equals(other)) {
            return false
        }
        other as PassiveDataTopic
        return processingState == other.processingState
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), processingState)
    }
}
