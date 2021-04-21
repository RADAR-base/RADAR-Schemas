package org.radarbase.schema.specification;

import static org.radarbase.schema.util.SchemaUtils.expandClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
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
