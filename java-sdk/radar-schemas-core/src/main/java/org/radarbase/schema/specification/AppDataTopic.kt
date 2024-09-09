package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import org.radarbase.config.OpenConfig
import org.radarbase.schema.util.SchemaUtils

@JsonInclude(NON_NULL)
@OpenConfig
class AppDataTopic : DataTopic() {
    @JsonProperty("app_provider")
    @set:JsonSetter
    var appProvider: String? = null
        set(value) {
            field = SchemaUtils.expandClass(value)
        }

    override fun propertiesMap(map: MutableMap<String, Any?>, reduced: Boolean) {
        map["app_provider"] = appProvider
        super.propertiesMap(map, reduced)
    }

    class DataField {
        @JsonProperty
        var name: String? = null
    }
}
