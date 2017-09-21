package org.radarcns.schema.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.Scope;
import org.radarcns.topic.AvroTopic;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.radarcns.schema.util.Utils.applyOrEmpty;

public abstract class DataProducer<T extends DataTopic> {
    @JsonProperty @NotBlank
    private String name;

    @JsonProperty @NotBlank
    private String doc;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private List<String> labels;

    public String getName() {
        return name;
    }

    public String getDoc() {
        return doc;
    }

    @NotNull
    public abstract List<T> getData();

    @NotNull
    public abstract Scope getScope();

    public List<String> getLabels() {
        return labels;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Stream<String> getTopicNames() {
        return getData().stream().flatMap(DataTopic::getTopicNames);
    }

    public Stream<AvroTopic<?, ?>> getTopics() {
        return getData().stream().flatMap(applyOrEmpty(DataTopic::getTopics));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataProducer producer = (DataProducer) o;
        return Objects.equals(name, producer.name)
                && Objects.equals(doc, producer.doc)
                && Objects.equals(getData(), producer.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, doc, getData());
    }
}
