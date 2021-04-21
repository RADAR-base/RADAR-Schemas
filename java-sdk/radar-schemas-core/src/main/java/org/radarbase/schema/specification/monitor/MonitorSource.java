package org.radarbase.schema.specification.monitor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.AppDataTopic;
import org.radarbase.schema.specification.AppSource;

@JsonInclude(Include.NON_NULL)
public class MonitorSource extends AppSource<AppDataTopic> {
    @JsonProperty
    private List<AppDataTopic> data;

    @Override
    public List<AppDataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.MONITOR;
    }
}
