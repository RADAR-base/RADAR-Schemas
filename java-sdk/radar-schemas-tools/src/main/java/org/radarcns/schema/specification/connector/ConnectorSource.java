package org.radarcns.schema.specification.connector;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.DataTopic;

import java.util.List;

/**
 * Data producer for third-party connectors. This data topic does not register schemas to the schema
 * registry by default, since Kafka Connect will do that itself. To enable auto-registration, set
 * the {@code register_schema} property to {@code true}.
 */
public class ConnectorSource extends DataProducer<DataTopic> {
    @JsonProperty
    private List<DataTopic> data;

    public ConnectorSource() {
        registerSchema = false;
    }

    @Override
    public List<DataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.CONNECTOR;
    }
}
