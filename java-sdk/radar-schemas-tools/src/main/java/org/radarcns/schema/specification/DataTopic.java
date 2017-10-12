package org.radarcns.schema.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.radarcns.catalogue.Unit;
import org.radarcns.config.AvroTopicConfig;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.topic.AvroTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static org.radarcns.schema.util.Utils.expandClass;

/** DataTopic topic from a data producer. */
public class DataTopic extends AvroTopicConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataTopic.class);

    @JsonProperty
    private String type;

    @JsonProperty
    private String doc;

    @JsonProperty("sample_rate")
    private SampleRateConfig sampleRate;

    @JsonProperty
    private Unit unit;

    @JsonProperty
    private List<AppDataTopic.DataField> fields;

    /**
     * DataTopic using ObservationKey as the default key.
     */
    public DataTopic() {
        // default value
        setKeySchema(ObservationKey.class.getName());
    }

    /** Get all topic names that are provided by the data. */
    public Stream<String> getTopicNames() {
        return Stream.of(getTopic());
    }

    /** Get all Avro topics that are provided by the data. */
    public Stream<AvroTopic<?, ?>> getTopics() throws IOException {
        try {
            return Stream.of(parseAvroTopic());
        } catch (IllegalArgumentException ex) {
            throw new IOException("Cannot parse Avro Topic " + getTopic()
                    + " schemas, with key_schema " + getKeySchema()
                    + " and value_schema " + getValueSchema(), ex);
        }
    }

    public String getType() {
        return type;
    }

    public String getDoc() {
        return doc;
    }

    public SampleRateConfig getSampleRate() {
        return sampleRate;
    }

    public Unit getUnit() {
        return unit;
    }

    public List<AppDataTopic.DataField> getFields() {
        return fields;
    }

    @Override
    @JsonSetter
    public void setKeySchema(String schema) {
        super.setKeySchema(expandClass(schema));
    }

    @Override
    @JsonSetter
    public void setValueSchema(String schema) {
        super.setValueSchema(expandClass(schema));
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean prettyString) {
        String name = getClass().getSimpleName();
        // preserves insertion order
        Map<String, Object> properties = new LinkedHashMap<>();
        propertiesMap(properties, !prettyString);

        if (prettyString) {
            Map<String, Object> completeMap = Collections.singletonMap(name, properties);
            YAMLFactory factory = new YAMLFactory();
            factory.configure(WRITE_DOC_START_MARKER, false);
            factory.configure(MINIMIZE_QUOTES, true);
            ObjectMapper mapper = new ObjectMapper(factory);
            try {
                return mapper.writeValueAsString(completeMap);
            } catch (JsonProcessingException ex) {
                logger.error("Failed to convert data to YAML", ex);
                return name + properties;
            }
        } else {
            return name + properties;
        }
    }

    protected void propertiesMap(Map<String, Object> map, boolean reduced) {
        map.put("type", type);
        if (!reduced && doc != null) {
            map.put("doc", doc);
        }

        String topic = getTopic();
        if (topic != null) {
            map.put("topic", topic);
        }
        map.put("key_schema", getKeySchema());
        map.put("value_schema", getValueSchema());

        if (!reduced) {
            if (sampleRate != null) {
                map.put("sample_rate", sampleRate);
            }
            if (unit != null) {
                map.put("unit", unit);
            }
            if (fields != null) {
                map.put("fields", fields);
            }
        }
    }
}
