package org.radarcns.schema.specification.active;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.radarcns.schema.specification.AppDataTopic;

import static org.radarcns.schema.util.Utils.expandClass;

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
