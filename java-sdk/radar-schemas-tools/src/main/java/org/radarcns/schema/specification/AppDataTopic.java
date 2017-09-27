package org.radarcns.schema.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

import static org.radarcns.schema.util.Utils.expandClass;

public class AppDataTopic extends DataTopic {
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

    @Override
    protected void propertiesMap(Map<String, Object> map, boolean reduced) {
        map.put("app_provider", appProvider);
        super.propertiesMap(map, reduced);
    }

    public static class DataField {
        @JsonProperty
        private String name;

        public String getName() {
            return name;
        }
    }
}
