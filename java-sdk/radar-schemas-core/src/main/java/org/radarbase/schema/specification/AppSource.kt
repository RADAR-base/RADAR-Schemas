package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import org.radarbase.config.OpenConfig
import org.radarbase.schema.util.SchemaUtils
import java.util.Objects

@JsonInclude(NON_NULL)
@OpenConfig
abstract class AppSource<T : DataTopic> : DataProducer<T>() {
    @JsonProperty("app_provider")
    @set:JsonSetter
    var appProvider: String? = null
        set(value) {
            field = SchemaUtils.expandClass(value)
        }

    @JsonProperty
    var vendor: String? = null

    @JsonProperty
    var model: String? = null

    @JsonProperty
    var version: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as AppSource<*>
        return appProvider == other.appProvider &&
            version == other.version &&
            model == other.model &&
            vendor == other.vendor &&
            data == other.data
    }

    override fun hashCode(): Int {
        return Objects.hash(appProvider, vendor, model, version, data)
    }
}
