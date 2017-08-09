package org.radarcns.validator;

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

import static org.junit.Assert.assertTrue;
import static org.radarcns.validator.util.AvroValidator.getExtension;

import java.io.File;
import java.io.IOException;
import org.radarcns.validator.SchemaCatalogValidator.SpecificationFolder;
import org.radarcns.validator.config.SkipConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public final class SpecificationsValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationsValidator.class);

    public static final String YML_FORMAT = "yml";

    private SpecificationsValidator() {
        //Static class
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void validateAll() {
        active();
        monitor();
        passive();
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void active() {
        analiseFiles(SpecificationFolder.ACTIVE.getFolder());
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void monitor() {
        analiseFiles(SpecificationFolder.MONITOR.getFolder());
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void passive() {
        analiseFiles(SpecificationFolder.PASSIVE.getFolder());
    }

    private static void analiseFiles(File file) {
        if (file.isDirectory()) {
            for (File son : file.listFiles()) {
                analiseFiles(son);
            }
        } else if (SkipConfig.skipFile(file)) {
            LOGGER.debug("Skipping {}", file.getAbsolutePath());
        } else {
            assertTrue(isYmlFile(file));
        }
    }

    /**
     * TODO.
     * @param file TODO
     * @return TODO
     */
    public static boolean isYmlFile(File file) {
        return YML_FORMAT.equals(getExtension(file));
    }
}
