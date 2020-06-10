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

package org.radarcns.schema.validation.config;

import static java.util.function.Predicate.not;
import static org.radarcns.schema.validation.rules.Validator.matches;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.radarcns.schema.validation.rules.SchemaField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public class ExcludeConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExcludeConfig.class);

    /** Repository name. */
    public static final String REPOSITORY_NAME = "/RADAR-Schemas/";

    /** File path location. */
    private static final String FILE_NAME = "schema.yml";

    /** Wild card to suppress check for entire package. */
    private static final String WILD_CARD_PACKAGE = ".*";

    /** Regex for validating the yml file. */
    public static final Pattern VALID_INPUT_PATTERN = Pattern.compile("[a-z][a-zA-Z0-9.*]*");

    @JsonIgnore
    private final Collection<PathMatcher> matchers = new ArrayList<>();
    private final Map<String, ConfigItem> validation = new HashMap<>();
    private Path root;

    /** Load the ExcludeConfig from file. */
    public static ExcludeConfig load(Path path) throws IOException {
        YAMLFactory factory = new YAMLFactory();
        ObjectReader reader = new ObjectMapper(factory)
                .readerFor(ExcludeConfig.class);
        if (path == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try (InputStream in = loader.getResourceAsStream(FILE_NAME)) {
                if (in == null) {
                    logger.debug("Not loading any configuration");
                    return new ExcludeConfig();
                } else {
                    return reader.readValue(in);
                }
            }
        } else {
            return reader.readValue(path.toFile());
        }
    }

    /**
     * TODO.
     * @param stream TODO
     * @return TODO
     */
    private static boolean invalidClass(Stream<String> stream) {
        return stream.anyMatch(not(matches(VALID_INPUT_PATTERN)));
    }

    /**
     * TODO.
     * @return TODO
     */
    public boolean isSkipped(SchemaField field) {
        Schema schema = field.getSchema();
        ConfigItem item = validation.get(schema.getFullName()) == null
                ? validation.get(schema.getNamespace() + WILD_CARD_PACKAGE)
                : validation.get(schema.getFullName());

        return item != null && item.getFields().contains(field.getField().name());
    }

    /**
     * TODO.
     * @param checkPath TODO
     * @return TODO
     */
    public boolean skipFile(Path checkPath) {
        if (checkPath == null) {
            return false;
        }
        Path relativePath = relativize(checkPath);

        return matchers.stream()
                .anyMatch(p -> p.matches(relativePath)
                        || p.matches(checkPath.getFileName()));
    }

    private Path relativize(Path path) {
        if (path.isAbsolute() && root != null) {
            try {
                return root.relativize(path.normalize());
            } catch (IllegalArgumentException ex) {
                // relativePath cannot be relativized
            }
        }
        return path;
    }

    public void setFiles(String... files) {
        setFiles(List.of(files));
    }

    /** Set the files to be excluded. */
    @JsonSetter("files")
    @SuppressWarnings("PMD.CloseResource")  // File system should not be closed
    public void setFiles(Collection<String> files) {
        FileSystem fs = FileSystems.getDefault();
        List<PathMatcher> pathMatchers = files.stream()
                .map(p -> {
                    try {
                        return fs.getPathMatcher("glob:" + p);
                    } catch (IllegalArgumentException ex) {
                        logger.error("Exclude pattern {} is invalid. Please use the "
                                + "glob syntax"
                                + "described in "
                                + "https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html"
                                + "#getPathMatcher(java.lang.String)", p, ex);
                        return null;
                    }
                }).collect(Collectors.toList());

        if (pathMatchers.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid exclude config.");
        }
        if (!files.isEmpty()) {
            this.matchers.clear();
        }
        this.matchers.addAll(pathMatchers);
    }

    /** Set the validation to be excluded. */
    @JsonSetter("validation")
    public void setValidation(Map<String, ConfigItem> validation) {
        //Validate validation key map
        if (invalidClass(validation.keySet().stream())) {
            throw new IllegalArgumentException("Validation map keys are invalid");
        }

        if (invalidClass(validation.values().stream()
                        .map(ConfigItem::getFields)
                        .flatMap(Set::stream))) {
            throw new IllegalArgumentException("Validation map values are not valid.");
        }

        if (!this.validation.isEmpty()) {
            this.validation.clear();
        }
        this.validation.putAll(validation);
    }

    public Map<String, ConfigItem> getValidation() {
        return new HashMap<>(validation);
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root.normalize();
    }
}
