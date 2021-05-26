package org.radarbase.schema.specification;

import static org.radarbase.schema.util.SchemaUtils.expandClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public abstract class AppSource<T extends DataTopic> extends DataProducer<T> {
    @JsonProperty("app_provider")
    private String appProvider;

    @JsonProperty
    private String vendor;

    @JsonProperty
    private String model;

    @JsonProperty
    private String version;

    @JsonSetter
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setAppProvider(String provider) {
        this.appProvider = expandClass(provider);
    }

    public String getAppProvider() {
        return appProvider;
    }

    public String getVersion() {
        return version;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppSource provider = (AppSource) o;
        return Objects.equals(appProvider, provider.appProvider)
                && Objects.equals(version, provider.version)
                && Objects.equals(model, provider.model)
                && Objects.equals(vendor, provider.vendor)
                && Objects.equals(getData(), provider.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(appProvider, vendor, model, version, getData());
    }
}
