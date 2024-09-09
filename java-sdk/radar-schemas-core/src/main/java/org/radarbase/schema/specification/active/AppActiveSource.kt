package org.radarbase.schema.specification.active

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import org.radarbase.config.OpenConfig
import org.radarbase.schema.specification.AppDataTopic
import org.radarbase.schema.util.SchemaUtils

@JsonInclude(NON_NULL)
@OpenConfig
class AppActiveSource : ActiveSource<AppDataTopic>() {
    @JsonProperty("app_provider")
    @set:JsonSetter
    var appProvider: String? = null
        set(value) {
            field = SchemaUtils.expandClass(value)
        }
}
