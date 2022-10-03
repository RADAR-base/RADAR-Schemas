package org.radarbase.schema.validation;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.schema.validation.config.ExcludeConfig.VALID_INPUT_PATTERN;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.radarbase.schema.validation.config.ExcludeConfig;

/**
 * TODO.
 */
public class ExcludeConfigTest {
    @Test
    public void testSkipFile() {
        ExcludeConfig config = new ExcludeConfig();
        String root = "/Users/developer/Repositories/RADAR-Schemas/";

        config.setRoot(Paths.get(root));
        config.setFiles(".DS_Store");

        assertTrue(config.skipFile(Paths.get(root, "commons/passive/.DS_Store")));
        assertTrue(config.skipFile(Paths.get(root, ".DS_Store")));

        config.setFiles("*.md");

        Path empaticaReadmePath = Paths.get(root, "commons/passive/empatica/README.md");
        assertTrue(config.skipFile(empaticaReadmePath));
        assertFalse(config.skipFile(Paths.get("specification/passive/schema.avsc")));

        config.setFiles("commons/**/*.md");

        assertTrue(config.skipFile(empaticaReadmePath));
        Path passiveReadmePath = Paths.get(root, "specification/passive/README.md");
        assertFalse(config.skipFile(passiveReadmePath));

        config.setFiles("commons/**/README.md");

        assertTrue(config.skipFile(empaticaReadmePath));
        assertFalse(config.skipFile(passiveReadmePath));

        config.setFiles(
                "commons/monitor/application/application_external_time.avsc");
        Path externalTimePath = Paths.get(root, "commons/monitor/application/"
                + "application_external_time.avsc");
        assertTrue(config.skipFile(externalTimePath));
        assertFalse(config.skipFile(Paths.get(root, "commons/passive/application/"
                + "application_external_time.avsc")));
        assertFalse(config.skipFile(Paths.get(root, "commons/monitor/application/"
                + "application_record_counts.avsc")));

        config.setFiles(
                "commons/monitor/application/application_external_time.avsc",
                "commons/**/*.avsc");
        assertTrue(config.skipFile(externalTimePath));
        assertFalse(config.skipFile(Paths.get(root, "restApi/data/acceleration.avsc")));
    }

    @Test
    public void testGeneralRegex() {
        assertTrue(VALID_INPUT_PATTERN.matcher("avg").matches());
        assertTrue(VALID_INPUT_PATTERN.matcher("x").matches());
        assertTrue(VALID_INPUT_PATTERN.matcher("org.radarcns.passive.phone.PhoneCall").matches());
        assertTrue(VALID_INPUT_PATTERN.matcher("org.radarcns.active.*").matches());
        assertFalse(VALID_INPUT_PATTERN
                .matcher("org.radarcns.passive.phone.PhoneCall as ENUM").matches());
        assertFalse(VALID_INPUT_PATTERN.matcher("*").matches());
    }
}
