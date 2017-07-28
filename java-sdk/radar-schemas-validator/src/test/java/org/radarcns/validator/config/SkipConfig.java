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
import java.util.Objects;
import java.util.Set;
import org.apache.avro.Schema;
import org.radarcns.config.YamlConfigLoader;

/**
 * TODO.
 */
public class SkipConfig {

    /** Repository name. */
    public static final String REPOSITORY_NAME = "/RADAR-Schemas/";

    /** File path location. */
    private static final String FILE_NAME = "skip.yml";

    /** Wild card to suppress check for entire package. */
    private static final String WILD_CARD_PACKAGE = ".*";

    /** Wild card to suppress check for folder and subfolders. */
    private static final String WILD_CARD_FOLDER = "**";

    private final Map<String, SkipConfigItem> setup = new HashMap<>();
    private final Set<Path> files = new HashSet<>();

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
     * @param schema TODO
     * @return TODO
     */
    public static boolean contains(Schema schema) {
        return CONFIG.setup.containsKey(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                || CONFIG.setup.containsKey(schema.getFullName());
    }

    /**
     * TODO.
     * @param schema TODO
     * @return TODO
     */
    public static boolean isNameRecordEnable(Schema schema) {
        SkipConfigItem item = CONFIG.setup.get(schema.getFullName()) == null
                ? CONFIG.setup.get(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                : CONFIG.setup.get(schema.getFullName());

        return item == null || item.isNameRecordDisable();
    }

    /**
     * TODO.
     * @param schema TODO
     * @return TODO
     */
    public static Set<String> skippedNameFieldCheck(Schema schema) {
        SkipConfigItem item = CONFIG.setup.get(schema.getFullName()) == null
                ? CONFIG.setup.get(schema.getNamespace().concat(WILD_CARD_PACKAGE))
                : CONFIG.setup.get(schema.getFullName());

        return item == null ? new HashSet<>() : item.getFields();
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean skipFile(File file) {
        Objects.requireNonNull(file);

        String path = file.toPath().toString().substring(
                file.toPath().toString().indexOf(REPOSITORY_NAME));
        path = path.substring(REPOSITORY_NAME.length());
        String[] components = path.split(File.separator);

        boolean flag = false;

        for (Path pathConfig : CONFIG.files) {
            if (pathConfig.startsWith(components[0])) {
                String[] tempComp = pathConfig.toString().split(File.separator);

                boolean match = true;

                for (int i = 1; i < tempComp.length; i++) {
                    if (!tempComp[i].equalsIgnoreCase(components[i])) {
                        match = false;
                        if (tempComp[i].equalsIgnoreCase(WILD_CARD_FOLDER)) {
                            if (i + 1 < tempComp.length) {
                                if (tempComp[i + 1].startsWith("*.")) {
                                    //Case file format
                                    flag = flag || getExtension(file).equalsIgnoreCase(
                                        tempComp[i + 1].substring(2));
                                } else if (tempComp[i + 1].contains(".")) {
                                    //Case file name
                                    flag = flag || file.getName().matches(tempComp[i + 1]);
                                }
                            } else {
                                flag = flag || true;
                            }
                        }
                    }
                }

                //Case exact matching
                flag = flag || match;
            }
        }

        return flag;
    }
}
