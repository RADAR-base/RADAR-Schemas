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

import org.junit.Test;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.validation.config.ExcludeConfig.VALID_INPUT_REGEX;

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

        assertTrue(config.skipFile(Paths.get(root, "commons/passive/empatica/README.md")));
        assertFalse(config.skipFile(Paths.get("specification/passive/schema.avsc")));

        config.setFiles("commons/**/*.md");

        assertTrue(config.skipFile(Paths.get(root, "commons/passive/empatica/README.md")));
        assertFalse(config.skipFile(Paths.get(root, "specification/passive/README.md")));

        config.setFiles("commons/**/README.md");

        assertTrue(config.skipFile(Paths.get(root, "commons/passive/empatica/README.md")));
        assertFalse(config.skipFile(Paths.get(root, "specification/passive/README.md")));

        config.setFiles(
                "commons/monitor/application/application_external_time.avsc");
        assertTrue(config.skipFile(Paths.get(root, "commons/monitor/application/"
                + "application_external_time.avsc")));
        assertFalse(config.skipFile(Paths.get(root, "commons/passive/application/"
                + "application_external_time.avsc")));
        assertFalse(config.skipFile(Paths.get(root, "commons/monitor/application/"
                + "application_record_counts.avsc")));

        config.setFiles(
                "commons/monitor/application/application_external_time.avsc",
                "commons/**/*.avsc");
        assertTrue(config.skipFile(Paths.get(root, "commons/monitor/application/"
                + "application_external_time.avsc")));
        assertFalse(config.skipFile(Paths.get(root, "restApi/data/acceleration.avsc")));
    }

    @Test
    public void testGeneralRegex() {
        assertTrue("avg".matches(VALID_INPUT_REGEX));
        assertTrue("x".matches(VALID_INPUT_REGEX));
        assertTrue("org.radarcns.passive.phone.PhoneCall".matches(VALID_INPUT_REGEX));
        assertTrue("org.radarcns.active.*".matches(VALID_INPUT_REGEX));
        assertFalse("org.radarcns.passive.phone.PhoneCall as ENUM".matches(VALID_INPUT_REGEX));
        assertFalse("*".matches(VALID_INPUT_REGEX));
    }
}
