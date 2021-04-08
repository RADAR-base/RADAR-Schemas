package org.radarbase.schema.specification.push;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.DataProducer;
import org.radarbase.schema.specification.DataTopic;

public class PushSource extends DataProducer<DataTopic> {

    @JsonProperty
    private List<DataTopic> data;

    @JsonProperty
    private String vendor;

    @JsonProperty
    private String model;

    @JsonProperty
    private String version;

    @Override
    public @NotNull List<DataTopic> getData() {
        return data;
    }

    @Override
    public @NotNull Scope getScope() {
        return Scope.PUSH;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }
}
