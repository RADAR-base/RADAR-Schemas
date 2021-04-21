package org.radarbase.schema.registration;

import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_COMPACT;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistryKey;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistryKeyType;
import io.confluent.kafka.schemaregistry.storage.SchemaRegistryValue;
import io.confluent.kafka.schemaregistry.storage.SchemaValue;
import io.confluent.kafka.schemaregistry.storage.exceptions.SerializationException;
import io.confluent.kafka.schemaregistry.storage.serialization.SchemaRegistrySerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Data class for containing the data and metadata of the _schemas topic.
 */
public class SchemaTopicBackup {

    @JsonProperty
    private final Map<String, String> settings;
    @JsonProperty
    private final Set<SchemaRecord> records;

    /**
     * Empty backup.
     */
    public SchemaTopicBackup() {
        settings = new HashMap<>();
        records = new LinkedHashSet<>();
    }

    /**
     * Fully ready backup.
     */
    @JsonCreator
    public SchemaTopicBackup(
            @JsonProperty("settings") Map<String, String> settings,
            @JsonProperty("records") List<SchemaRecord> records) {
        this.settings = new HashMap<>(settings);
        this.records = new LinkedHashSet<>(records);
    }

    /**
     * Whether the backup contains schemas starting from ID 1. If so, the schema topic is valid.
     */
    public boolean startsAtFirstId() {
        return records.stream()
                .map(SchemaRecord::getSchemaId)
                .filter(Objects::nonNull)
                .anyMatch(r -> r == 1);
    }

    @JsonGetter
    @NotNull
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * Put settings.
     *
     * @param settings Kafka topic config settings
     */
    public void putAll(@NotNull Map<? extends String, ? extends String> settings) {
        this.settings.putAll(settings);
        if (!Objects.equals(settings.get(CLEANUP_POLICY_CONFIG), CLEANUP_POLICY_COMPACT)) {
            this.settings.put(CLEANUP_POLICY_CONFIG, CLEANUP_POLICY_COMPACT);
        }
    }

    /**
     * Add schema from topic. This will deduplicate using record key.
     */
    public void addSchemaRecord(
            SchemaRegistrySerializer serializer,
            ConsumerRecord<byte[], byte[]> record) throws IOException {

        SchemaRecord schemaRecord = null;
        SchemaRegistryKey messageKey = null;
        try {
            messageKey = serializer.deserializeKey(record.key());

            if (messageKey.getKeyType() == SchemaRegistryKeyType.SCHEMA
                    && record.value() != null) {
                SchemaRegistryValue message = serializer.deserializeValue(
                        messageKey, record.value());

                if (message instanceof SchemaValue) {
                    SchemaValue schemaValue = (SchemaValue) message;
                    schemaRecord = new SchemaRecord(
                            messageKey.getKeyType(),
                            schemaValue.getSubject(),
                            schemaValue.getId(),
                            record.key(),
                            record.value());
                }
            }
        } catch (SerializationException ex) {
            throw new IOException("Cannot deserialize message", ex);
        }
        if (schemaRecord == null) {
            schemaRecord = new SchemaRecord(
                    messageKey.getKeyType(),
                    null,
                    null,
                    record.key(),
                    record.value());
        }

        // preserve order
        records.remove(schemaRecord);
        records.add(schemaRecord);
    }

    @JsonGetter
    @NotNull
    public List<SchemaRecord> getRecords() {
        return new ArrayList<>(records);
    }

    /**
     * Get the Kafka config of a topic.
     *
     * @return configuration of the topic. It may be empty if not initialized.
     */
    @JsonIgnore
    @NotNull
    public Config getConfig() {
        return new Config(settings.entrySet().stream()
                .map(e -> new ConfigEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    /**
     * Set the Kafka config of a topic.
     *
     * @param config configuration of the topic.
     */
    @JsonIgnore
    public void setConfig(@NotNull Config config) {
        putAll(config.entries().stream()
                .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value)));
    }

    /**
     * Schema record.
     */
    public static class SchemaRecord {

        private final SchemaRegistryKeyType type;
        private final Integer schemaId;
        private final String subject;
        private final byte[] key;
        private final byte[] value;

        /**
         * Full constructor of all properties.
         *
         * @param type record type
         * @param subject schema subject that the record belongs to, or null if not a schema.
         * @param schemaId schema ID or null if not a schema.
         * @param key raw key data
         * @param value raw value data
         */
        @JsonCreator
        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        public SchemaRecord(
                @NotNull @JsonProperty("type") SchemaRegistryKeyType type,
                @JsonProperty("subject") String subject,
                @JsonProperty("schemaId") Integer schemaId,
                @NotNull @JsonProperty("key") byte[] key,
                @NotNull @JsonProperty("value") byte[] value) {
            this.type = type;
            this.schemaId = schemaId;
            this.subject = subject;
            this.key = key;
            this.value = value;
        }

        public SchemaRegistryKeyType getType() {
            return type;
        }

        public Integer getSchemaId() {
            return schemaId;
        }

        public String getSubject() {
            return subject;
        }

        public byte[] getKey() {
            return Arrays.copyOf(key, key.length);
        }

        public byte[] getValue() {
            return Arrays.copyOf(value, value.length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SchemaRecord that = (SchemaRecord) o;
            return Arrays.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(key);
        }
    }
}
