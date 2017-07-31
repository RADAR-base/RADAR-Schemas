package org.radarcns.validator.config;

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

import static org.radarcns.validator.util.AvroValidator.getExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.radarcns.config.YamlConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public class SkipConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkipConfig.class);

    /** Repository name. */
    public static final String REPOSITORY_NAME = "/RADAR-Schemas/";

    /** File path location. */
    private static final String FILE_NAME = "skip.yml";

    /** Wild card to suppress check for entire package. */
    public static final String WILD_CARD_PACKAGE = ".*";

    /** Wild card to suppress check for all file extension. */
    public static final String WILD_CARD_FILE_NAME = "*.";

    /** Wild card to suppress check for folder and subfolders. */
    public static final String WILD_CARD_FOLDER = "**";

    /** Wild card to suppress check all schemas. */
    public static final String WILD_CARD_COLLISION = "*";

    /** Regex for validating the yml file. */
    protected static final String VALID_INPUT_REGEX = "^[a-z][a-zA-Z0-9.*]*$";
    protected static final String VALID_FILE_REGEX = "^[a-zA-Z0-9.*][a-zA-Z0-9._*\\/\\\\]+";

    private final Set<Path> files = new HashSet<>();
    private final Map<String, SkipConfigItem> validation = new HashMap<>();
    private final Map<String, Set<String>> collision = new HashMap<>();

    private static final SkipConfig CONFIG;

    public SkipConfig() {
        // POJO initializer
    }

    static {
        try {
            CONFIG = new YamlConfigLoader().load(new File(
                SkipConfig.class.getClassLoader().getResource(FILE_NAME).getFile()),
                SkipConfig.class);
        } catch (IOException exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * TODO.
     * @return TODO
     */
    public static boolean validate() {
        boolean valid = validateClass(CONFIG.files.stream().map(Path::toString),
                "Skipped file list is not valid.", true);

        //Validate validation key map
        valid = valid && validateClass(CONFIG.validation.keySet().stream(),
                "Validation map keys are not valid.", false);

        //Validate validation value map
        valid = valid && validateClass(CONFIG.validation.values().stream()
                                          .map(skipConfigItem -> skipConfigItem.getFields())
                                          .flatMap(Set::stream),
                                      "Validation map values are not valid.", false);

        //Validate collision key map
        valid = valid && validateClass(CONFIG.collision.keySet().stream(),
                "Collision map keys are not valid.", false);

        //Validate collision value map
        return valid && validateClass(CONFIG.collision.values().stream().flatMap(Set::stream),
                "Collision map values are not valid.", false);
    }

    /**
     * TODO.
     * @param stream TODO
     * @param message TODO
     * @param file TODO
     * @return TODO
     */
    private static boolean validateClass(Stream<String> stream, String message, boolean file) {
        return stream.allMatch(value -> {
            if (file && !value.matches(VALID_FILE_REGEX)
                    || !file && !value.matches(VALID_INPUT_REGEX)) {
                LOGGER.error("{} Setting \"{}\" is invalid.", message, value);
                return false;
            }
            return true;
        });
    }

    /**
     * TODO.
     * @param schema TODO
     * @return TODO
     */
    public static boolean contains(Schema schema) {
        return CONFIG.validation.containsKey(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                || CONFIG.validation.containsKey(schema.getFullName());
    }

    /**
     * TODO.
     * @param schema TODO
     * @return TODO
     */
    public static boolean isNameRecordEnable(Schema schema) {
        SkipConfigItem item = CONFIG.validation.get(schema.getFullName()) == null
                ? CONFIG.validation.get(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                : CONFIG.validation.get(schema.getFullName());

        return item == null || item.isNameRecordDisable();
    }

    /**
     * TODO.
     * @param schema TODO
     * @return TODO
     */
    public static Set<String> skippedNameFieldCheck(Schema schema) {
        SkipConfigItem item = CONFIG.validation.get(schema.getFullName()) == null
                ? CONFIG.validation.get(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                : CONFIG.validation.get(schema.getFullName());

        return item == null ? new HashSet<>() : item.getFields();
    }

    /**
     * TODO.
     * @param schema TODO
     * @param field TODO
     * @return TODO
     */
    public static boolean skipCollision(Schema schema, Field field) {
        Set<String> info = CONFIG.collision.getOrDefault(field.name(), new HashSet<>());
        if (info.contains(schema.getFullName()) || info.contains(WILD_CARD_COLLISION)) {
            return true;
        }

        for (String value : info) {
            if (value.contains(WILD_CARD_PACKAGE) && schema.getNamespace().startsWith(
                    value.substring(0, value.length() - 2))) {
                return true;
            }
        }

        return false;
    }

    /**
     * TODO.
     * @param schema TODO
     * @param field TODO
     * @return TODO
     */
    public static Set<String> getCollision(Schema schema, Field field) {
        Set<String> set = CONFIG.collision.getOrDefault(field.name(), new HashSet<>());
        set.add(schema.getFullName());
        return set;
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean skipFile(File file) {
        return skipFile(file, CONFIG.files);
    }

    /**
     * TODO.
     * @param file TODO
     * @param pathToSkip TODO
     * @return TODO
     */
    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
    //TODO simplify function
    protected static boolean skipFile(File file, Set<Path> pathToSkip) {
        boolean flag = false;

        String pathString = file.toPath().toString().substring(file.toPath().toString().indexOf(
                REPOSITORY_NAME));
        pathString = pathString.substring(REPOSITORY_NAME.length());
        String[] components = pathString.split(File.separator);

        for (Path path : pathToSkip) {
            if (path.getFileName().equals(path)) {
                if (path.toString().startsWith(WILD_CARD_FILE_NAME)) {
                    //Case extension
                    flag = flag || getExtension(file).equals(path.toString().substring(2));
                } else {
                    //Case file name
                    flag = flag || file.getName().equals(path.getFileName().toString());
                }
            } else {
                if (pathString.equals(path.toString())) {
                    //Exact match
                    flag = true;
                } else {
                    //Search sub-path
                    String[] configComp = path.toString().split(File.separator);

                    for (int i = 0; i < Math.min(configComp.length, components.length); i++) {
                        if (!components[i].equals(configComp[i])) {
                            if (configComp[i].equals(WILD_CARD_FOLDER)) {
                                if (i == configComp.length - 1) {
                                    //Case folder and subfolder file independent
                                    flag = true;
                                } else {
                                    if (configComp[i + 1].startsWith(WILD_CARD_FILE_NAME)) {
                                        //Case extension
                                        flag = flag || getExtension(file).equals(
                                            configComp[i + 1].substring(2));
                                    } else {
                                        //Case name
                                        flag = flag || file.getName().equals(configComp[i + 1]);
                                    }
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }

        return flag;
    }
}
