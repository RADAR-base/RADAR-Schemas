package org.radarcns.schema.specification.monitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.AppDataTopic;
import org.radarcns.schema.specification.AppSource;

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
