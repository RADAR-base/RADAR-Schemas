package org.radarcns.schema.validation;

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

import org.apache.avro.Schema;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;
import org.radarcns.schema.validation.rules.RadarSchemaMetadataRules;
import org.radarcns.schema.validation.rules.RadarSchemaRules;
import org.radarcns.schema.validation.rules.SchemaMetadata;
import org.radarcns.schema.validation.rules.SchemaMetadataRules;
import org.radarcns.schema.validation.rules.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static org.radarcns.schema.SchemaRepository.COMMONS_PATH;
import static org.radarcns.schema.validation.rules.Validator.raise;

public class SchemaValidator {
    public static final String AVRO_EXTENSION = "avsc";

    private final Path root;
    private final ExcludeConfig config;
    private final Validator<SchemaMetadata> validator;
    private final SchemaMetadataRules rules;

    public SchemaValidator(Path root, ExcludeConfig config) {
        this.config = config;
        this.root = root;
        this.rules = new RadarSchemaMetadataRules(root, config);
        this.validator = rules.getValidator();
    }

    /**
     * TODO.
     * @param scope TODO.
     */
    public Stream<ValidationException> analyseFiles(Scope scope) {
        try {
            List<Path> avroFiles = Files.walk(scope.getPath(root.resolve(COMMONS_PATH)))
                    .filter(Files::isRegularFile)
                    .filter(p -> !config.skipFile(p))
                    .collect(Collectors.toList());

            Map<Path, Schema> enums = avroFiles.stream()
                    .filter(SchemaValidator::isAvscFile)
                    .map(p -> {
                        try {
                            return new AbstractMap.SimpleImmutableEntry<>(p,
                                    new Schema.Parser().parse(p.toFile()));
                        } catch (Exception ex) {
                            return null;
                        }
                    })
                    .filter(s -> s != null && s.getValue().getType() == Schema.Type.ENUM)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> {
                        if (v1.equals(v2)) {
                            return v1;
                        } else {
                            throw new IllegalStateException("Duplicate enum: " + v1);
                        }
                    }));

            Collection<Path> skipEnums = enums.keySet();
            Map<String, Schema> useTypes = enums.values().stream()
                    .collect(Collectors.toMap(Schema::getFullName, identity()));

            return avroFiles.stream()
                    .filter(p -> !skipEnums.contains(p))
                    .flatMap(p -> {
                        if (!isAvscFile(p)) {
                            return raise(p.toAbsolutePath() + " is invalid. " + scope.getLower()
                                            + " should contain only " + AVRO_EXTENSION + " files.");
                        }

                        try {
                            Schema.Parser parser = new Schema.Parser();
                            parser.addTypes(useTypes);
                            Schema schema = parser.parse(p.toFile());

                            return validate(schema, p, scope);
                        } catch (IOException e) {
                            return raise("Cannot parse file " + p.toAbsolutePath(), e);
                        }
                    });
        } catch (IOException ex) {
            return raise("Failed to read files: " + ex, ex);
        }
    }


    /**
     * TODO.
     * @throws IOException TODO.
     */
    public Stream<ValidationException> analyseFiles()
            throws IOException {
        return Arrays.stream(Scope.values())
                .flatMap(this::analyseFiles);
    }

    public Stream<ValidationException> validate(Schema schema, Path path, Scope scope) {
        return validator.apply(new SchemaMetadata(schema, scope, path));
    }

    public static Stream<String> formatStream(Stream<ValidationException> exceptionStream) {
        return exceptionStream.map(ex -> "Validation FAILED:\n" + ex.getMessage() + "\n\n");
    }

    public static String format(Stream<ValidationException> exceptionStream) {
        return SchemaValidator.formatStream(exceptionStream)
                .collect(Collectors.joining());
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isAvscFile(Path file) {
        return ValidationSupport.matchesExtension(file, AVRO_EXTENSION);
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
