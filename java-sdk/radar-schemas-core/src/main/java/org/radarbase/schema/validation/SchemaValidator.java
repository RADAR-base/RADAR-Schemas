/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.schema.validation;

import kotlin.Pair;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;
import org.radarbase.schema.SchemaCatalogue;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.DataProducer;
import org.radarbase.schema.specification.SourceCatalogue;
import org.radarbase.schema.validation.config.ExcludeConfig;
import org.radarbase.schema.validation.rules.RadarSchemaMetadataRules;
import org.radarbase.schema.validation.rules.RadarSchemaRules;
import org.radarbase.schema.validation.rules.SchemaMetadata;
import org.radarbase.schema.validation.rules.SchemaMetadataRules;
import org.radarbase.schema.validation.rules.Validator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator for a set of RADAR-Schemas.
 */
public class SchemaValidator {
    public static final String AVRO_EXTENSION = "avsc";

    private final ExcludeConfig config;
    private final SchemaMetadataRules rules;
    private Validator<SchemaMetadata> validator;

    /**
     * Schema validator for given RADAR-Schemas directory.
     * @param root RADAR-Schemas directory.
     * @param config configuration to exclude certain schemas or fields from validation.
     */
    public SchemaValidator(Path root, ExcludeConfig config) {
        this.config = config;
        this.rules = new RadarSchemaMetadataRules(root, config);
        this.validator = rules.getValidator(false);
    }

    public Stream<ValidationException> analyseSourceCatalogue(
            Scope scope, SourceCatalogue catalogue) {
        this.validator = rules.getValidator(true);
        Stream<DataProducer<?>> producers;
        if (scope != null) {
            producers = catalogue.getSources().stream()
                    .filter(s -> s.getScope().equals(scope));
        } else {
            producers = catalogue.getSources().stream();
        }

        try {
            return producers.flatMap(s -> s.getData().stream())
                    .flatMap(d -> {
                        Pair<SchemaMetadata, SchemaMetadata> metadata =
                                catalogue.getSchemaCatalogue().getSchemaMetadata(d);
                        return Stream.of(metadata.component1(), metadata.component2());
                    })
                    .sorted(Comparator.comparing(s -> s.getSchema().getFullName()))
                    .distinct()
                    .flatMap(this::validate)
                    .distinct();
        } finally {
            this.validator = rules.getValidator(false);
        }
    }

    /**
     * TODO.
     * @param scope TODO.
     */
    public Stream<ValidationException> analyseFiles(Scope scope, SchemaCatalogue schemaCatalogue) {
        if (scope == null) {
            return analyseFiles(schemaCatalogue);
        }
        this.validator = rules.getValidator(false);
        Map<String, Schema> useTypes = schemaCatalogue.getSchemas().entrySet().stream()
                .filter(s -> s.getValue().getScope().equals(scope))
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getSchema()));

        return Stream.concat(
                schemaCatalogue.getUnmappedAvroFiles().stream()
                        .filter(s -> s.getScope().equals(scope))
                        .map(p -> {
                            Parser parser = new Parser();
                            parser.addTypes(useTypes);
                            try {
                                parser.parse(p.getPath().toFile());
                                return null;
                            } catch (Exception ex) {
                                return new ValidationException("Cannot parse schema", ex);
                            }
                        })
                        .filter(Objects::nonNull),
                schemaCatalogue.getSchemas().values().stream()
                        .flatMap(this::validate)
        ).distinct();
    }

    /**
     * TODO.
     */
    public Stream<ValidationException> analyseFiles(SchemaCatalogue schemaCatalogue) {
        return Arrays.stream(Scope.values())
                .flatMap(scope -> analyseFiles(scope, schemaCatalogue));
    }

    /** Validate a single schema in given path. */
    public Stream<ValidationException> validate(Schema schema, Path path, Scope scope) {
        return validate(new SchemaMetadata(schema, scope, path));
    }

    /** Validate a single schema in given path. */
    public Stream<ValidationException> validate(SchemaMetadata schemaMetadata) {
        if (config.skipFile(schemaMetadata.getPath())) {
            return Stream.empty();
        }
        return validator.apply(schemaMetadata);
    }

    /** Formats a stream of validation exceptions. */
    public static String format(Stream<ValidationException> exceptionStream) {
        return exceptionStream
                .map(ex -> "Validation FAILED:\n" + ex.getMessage() + "\n\n")
                .collect(Collectors.joining());
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isAvscFile(Path file) {
        return ValidationHelper.matchesExtension(file, AVRO_EXTENSION);
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static String getPath(Path path) {
        return path.toString().substring(path.toString().indexOf(ExcludeConfig.REPOSITORY_NAME));
    }

    public SchemaMetadataRules getRules() {
        return rules;
    }

    public Map<String, Schema> getValidatedSchemas() {
        return ((RadarSchemaRules) rules.getSchemaRules()).getSchemaStore();
    }
}
