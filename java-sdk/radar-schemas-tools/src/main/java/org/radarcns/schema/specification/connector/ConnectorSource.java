package org.radarcns.schema.specification.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.AppSource;

import java.util.List;

public class ConnectorSource extends AppSource<ConnectorDataTopic> {
    @JsonProperty
    private List<ConnectorDataTopic> data;

    @Override
    public List<ConnectorDataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.CONNECTOR;
    }
}
