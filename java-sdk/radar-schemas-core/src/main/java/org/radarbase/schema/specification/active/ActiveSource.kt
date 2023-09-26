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
package org.radarbase.schema.specification.active

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import jakarta.validation.constraints.NotBlank
import org.radarbase.schema.Scope
import org.radarbase.schema.Scope.ACTIVE
import org.radarbase.schema.specification.DataProducer
import org.radarbase.schema.specification.DataTopic
import org.radarbase.schema.specification.active.questionnaire.QuestionnaireSource

@JsonTypeInfo(use = NAME, property = "assessment_type")
@JsonSubTypes(
    value = [
        Type(
            name = "QUESTIONNAIRE",
            value = QuestionnaireSource::class,
        ), Type(name = "APP", value = AppActiveSource::class),
    ],
)
@JsonInclude(
    NON_NULL,
)
open class ActiveSource<T : DataTopic> : DataProducer<T>() {
    enum class RadarSourceTypes {
        QUESTIONNAIRE,
    }

    @JsonProperty("assessment_type")
    val assessmentType: @NotBlank String? = null

    @JsonProperty
    override val data: MutableList<T> = mutableListOf()

    @JsonProperty
    val vendor: String? = null

    @JsonProperty
    val model: String? = null

    @JsonProperty
    val version: String? = null
    override val scope: Scope = ACTIVE
}
