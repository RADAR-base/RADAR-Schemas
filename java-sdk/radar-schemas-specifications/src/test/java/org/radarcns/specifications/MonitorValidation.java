package org.radarcns.specifications;

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
import static org.radarcns.specifications.SourceCatalogue.BASE_PATH;
import static org.radarcns.specifications.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.specifications.validator.ValidationSupport.getMessage;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.specifications.SourceCatalogue.NameFolder;
import org.radarcns.specifications.source.passive.MonitorSource;
import org.radarcns.specifications.validator.ValidationResult;
import org.radarcns.specifications.validator.Validator;

/**
 * TODO.
 */
public class MonitorValidation {

    @Test
    public void validate() throws IOException {
        for (MonitorSourceType type : MonitorSourceType.values()) {

            if (type.name().equals(MonitorSourceType.UNKNOWN.name())) {
                continue;
            }

            File file = new File(BASE_PATH.resolve(
                    NameFolder.MONITOR.getName()).resolve(
                            type.name().toLowerCase().concat(YAML_EXTENSION)).toUri());

            MonitorSource source = new YamlConfigLoader().load(file, MonitorSource.class);

            ValidationResult result = Validator.validateMonitor(source, file);
            assertTrue(getMessage(file, result), result.isValid());
        }
    }

}
