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
package org.radarbase.schema.specification.active.questionnaire

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.config.OpenConfig
import org.radarbase.schema.specification.DataTopic
import java.net.URL

@JsonInclude(NON_NULL)
@OpenConfig
class QuestionnaireDataTopic : DataTopic() {
    @JsonProperty("questionnaire_definition_url")
    var questionnaireDefinitionUrl: URL? = null

    override fun propertiesMap(map: MutableMap<String, Any?>, reduced: Boolean) {
        super.propertiesMap(map, reduced)
        map["questionnaire_definition_url"] = questionnaireDefinitionUrl
    }
}
