package org.radarbase.schema.specification

import com.fasterxml.jackson.annotation.JsonProperty
import org.radarbase.config.OpenConfig

@OpenConfig
class SampleRateConfig {
    @JsonProperty
    var interval: Double? = null

    @JsonProperty
    var frequency: Double? = null

    @JsonProperty("dynamic")
    var isDynamic = false

    @JsonProperty("configurable")
    var isConfigurable = false

    override fun toString(): String {
        return (
            "SampleRateConfig{interval=" + interval +
                ", frequency=" + frequency +
                ", dynamic=" + isDynamic +
                ", configurable=" + isConfigurable +
                '}'
            )
    }
}
