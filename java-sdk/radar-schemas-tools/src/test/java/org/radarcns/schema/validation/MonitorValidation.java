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

import java.io.IOException;
import org.junit.Test;

/**
 * TODO.
 */
public class MonitorValidation {

    @Test
    public void validate() throws IOException {
        /*for (MonitorSourceType type : MonitorSourceType.values()) {

            if (type.name().equals(MonitorSourceType.UNKNOWN.name())) {
                continue;
            }

            File file = new File(BASE_PATH.resolve(
                    NameFolder.MONITOR.getLiteral()).resolve(
                            type.name().toLowerCase() + YAML_EXTENSION)).toUri());

            MonitorSource source = new YamlConfigLoader().load(file, MonitorSource.class);

            ValidationResult result = Validator.validateMonitor(source, file);
            assertTrue(getMessage(file, result), result.isEmpty());
        }*/
    }

}
