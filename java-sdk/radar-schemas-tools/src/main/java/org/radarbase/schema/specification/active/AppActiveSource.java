package org.radarbase.schema.specification.active;

import static org.radarbase.schema.util.SchemaUtils.expandClass;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.radarbase.schema.specification.AppDataTopic;

public class AppActiveSource extends ActiveSource<AppDataTopic> {
    @JsonProperty("app_provider")
    private String appProvider;

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setAppProvider(String provider) {
        this.appProvider = expandClass(provider);
    }

    public String getAppProvider() {
        return appProvider;
    }
}
