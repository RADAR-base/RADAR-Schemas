package org.radarcns.schema.specification.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.AppSource;

import java.util.List;

public class MonitorSource extends AppSource<MonitorDataTopic> {
    @JsonProperty
    private List<MonitorDataTopic> data;

    @Override
    public List<MonitorDataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.MONITOR;
    }
}
