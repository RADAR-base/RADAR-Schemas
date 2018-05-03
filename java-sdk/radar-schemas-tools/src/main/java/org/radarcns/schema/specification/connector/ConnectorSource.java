package org.radarcns.schema.specification.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.DataTopic;

import java.util.List;

public class ConnectorSource extends DataProducer<DataTopic> {
    @JsonProperty
    private List<DataTopic> data;

    @Override
    public List<DataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.CONNECTOR;
    }
}
