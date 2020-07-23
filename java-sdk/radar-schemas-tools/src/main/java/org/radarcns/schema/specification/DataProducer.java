package org.radarcns.schema.specification;

import static org.radarcns.schema.util.SchemaUtils.applyOrEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.radarcns.schema.Scope;
import org.radarbase.topic.AvroTopic;

/**
 * A producer of data to Kafka, generally mapping to a source.
 * @param <T> type of data that is produced.
 */
public abstract class DataProducer<T extends DataTopic> {
    @JsonProperty @NotBlank
    private String name;

    @JsonProperty @NotBlank
    private String doc;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private List<String> labels;

    /**
     * If true, register the schema during kafka initialization, otherwise, the producer should do
     * that itself. The default is true, set in the constructor of subclasses to use a different
     * default.
     */
    @JsonProperty("register_schema")
    protected boolean registerSchema = true;

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

    @JsonIgnore
    public Stream<String> getTopicNames() {
        return getData().stream().flatMap(DataTopic::getTopicNames);
    }

    @JsonIgnore
    public Stream<AvroTopic<?, ?>> getTopics() {
        return getData().stream().flatMap(applyOrEmpty(DataTopic::getTopics));
    }

    public boolean doRegisterSchema() {
        return registerSchema;
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
