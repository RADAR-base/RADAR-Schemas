package org.radarcns.schema.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import static org.radarcns.schema.util.Utils.expandClass;

public abstract class AppSource<T extends DataTopic> extends DataProducer<T> {
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
