package org.radarcns.schema.specification.stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.schema.specification.DataProducer;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Stream;

public class StreamGroup extends DataProducer<StreamDataTopic> {
    @JsonProperty @NotEmpty
    private List<StreamDataTopic> data;

    @JsonProperty
    private String master;

    @Override
    public List<StreamDataTopic> getData() {
        return data;
    }

    @Override
    public Scope getScope() {
        return Scope.STREAM;
    }

    /** Get only the topic names that are the output of a timed stream. */
    public Stream<String> getTimedTopicNames() {
        return data.stream().flatMap(StreamDataTopic::getTimedTopicNames);
    }

    public String getMaster() {
        return master;
    }
}
