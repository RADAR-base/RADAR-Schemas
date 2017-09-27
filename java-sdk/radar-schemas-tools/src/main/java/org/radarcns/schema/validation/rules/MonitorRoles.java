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

package org.radarcns.schema.validation.rules;

import org.radarcns.schema.specification.monitor.MonitorSource;
import org.radarcns.schema.util.Utils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.radarcns.schema.specification.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.schema.validation.ValidationSupport.equalsFileName;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;

/**
 * TODO.
 */
public final class MonitorRoles {
    private static final Set<String> ALLOWED_PROVIDERS = Collections.singleton(
            Utils.getProjectGroup() + ".application.ApplicationServiceProvider");

    private static final String APP_PROVIDER = "App provider should be equal to one of the"
            + " following values: " + ALLOWED_PROVIDERS + ".";
    private static final String SOURCE_TYPE = "Source type should match file name.";

    private MonitorRoles() {
        // utility class
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<MonitorSource> validateAppProvider() {
        return validateNonNull(MonitorSource::getAppProvider, ALLOWED_PROVIDERS::contains,
                APP_PROVIDER);
    }

    /**
     * TODO.
     * @return TODO
     */
    static Validator<MonitorSource> validateSourceType(Path file) {
        return validateNonNull(MonitorSource::getName, equalsFileName(file, YAML_EXTENSION),
                SOURCE_TYPE);
    }
}
