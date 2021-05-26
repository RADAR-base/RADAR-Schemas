package org.radarbase.schema;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.apache.avro.generic.GenericRecord;
import org.radarbase.config.AvroTopicConfig;
import org.radarbase.schema.validation.SchemaValidator;
import org.radarbase.schema.validation.rules.SchemaMetadata;
import org.radarbase.topic.AvroTopic;

public class SchemaCatalogue {
    private final Path root;
    private final Map<String, SchemaMetadata> schemas;
    private final List<Path> unmappedFiles;


    public SchemaCatalogue(Path root) throws IOException {
        this(root, null, p -> Files.isRegularFile(p) && SchemaValidator.isAvscFile(p));
    }

    public SchemaCatalogue(Path root, Scope scope, Predicate<Path> filterPath) throws IOException {
        this.root = root.resolve("commons");
        Map<String, SchemaMetadata> schemaTemp = new HashMap<>();
        List<Path> unmappedTemp = new ArrayList<>();

        loadSchemas(schemaTemp, unmappedTemp, scope, filterPath);

        schemas = Collections.unmodifiableMap(schemaTemp);
        unmappedFiles = Collections.unmodifiableList(unmappedTemp);
    }

    /**
     * Returns an avro topic with the schemas from this catalogue.
     * @param config avro topic configuration
     * @return AvroTopic with
     * @throws NoSuchElementException if the key or value schema do not exist in this catalogue.
     * @throws NullPointerException if the key or value schema configurations are null
     * @throws IllegalArgumentException if the topic configuration is null
     */
    public AvroTopic<GenericRecord, GenericRecord> getGenericAvroTopic(AvroTopicConfig config) {
        SchemaMetadata parsedKeySchema = schemas.get(config.getKeySchema());
        if (parsedKeySchema == null) {
            throw new NoSuchElementException("Key schema " + config.getKeySchema()
                    + " for topic " + config.getTopic() + " not found.");
        }

        SchemaMetadata parsedValueSchema = schemas.get(config.getValueSchema());
        if (parsedValueSchema == null) {
            throw new NoSuchElementException("Value schema " + config.getValueSchema()
                    + " for topic " + config.getTopic() + " not found.");
        }

        return new AvroTopic<>(config.getTopic(),
                parsedKeySchema.getSchema(), parsedValueSchema.getSchema(),
                GenericRecord.class, GenericRecord.class);
    }

    public Map<String, SchemaMetadata> getSchemas() {
        return schemas;
    }

    public List<Path> getUnmappedAvroFiles() {
        return unmappedFiles;
    }

    private void loadSchemas(
            Map<String, SchemaMetadata> schemas,
            List<Path> unmappedFiles,
            Scope scope,
            Predicate<Path> filterPath) throws IOException {

        Path walkRoot;
        if (scope != null) {
            walkRoot = scope.getPath(root);
            if (walkRoot == null) {
                throw new IllegalArgumentException("No scope directory for scope " + scope);
            }
        } else {
            walkRoot = root;
        }

        List<Path> avroFiles = Files.walk(walkRoot)
                .filter(filterPath)
                .collect(Collectors.toList());

        int prevSize = -1;

        // Recursively parse all schemas.
        // If the parsed schema size does not change anymore, the final schemas cannot be parsed
        // at all.
        while (prevSize != schemas.size()) {
            prevSize = schemas.size();
            Map<String, Schema> useTypes = schemas.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getSchema()));
            Set<Path> ignoreFiles = schemas.values().stream()
                    .map(SchemaMetadata::getPath)
                    .collect(Collectors.toSet());

            schemas.putAll(avroFiles.stream()
                    .filter(not(ignoreFiles::contains))
                    .map(p -> {
                        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                        Parser parser = new Parser();
                        parser.addTypes(useTypes);
                        try {
                            return new SchemaMetadata(parser.parse(p.toFile()), scope, p);
                        } catch (Exception ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            m -> m.getSchema().getFullName(),
                            identity(),
                            (v1, v2) -> {
                                if (v1.equals(v2)) {
                                    return v1;
                                } else {
                                    throw new IllegalStateException("Duplicate enum: " + v1);
                                }
                            })));
        }

        Set<Path> mappedPaths = schemas.values().stream()
                .map(SchemaMetadata::getPath)
                .collect(Collectors.toSet());

        unmappedFiles.addAll(avroFiles.stream()
                .filter(p -> !mappedPaths.contains(p))
                .collect(Collectors.toList()));
    }
}
