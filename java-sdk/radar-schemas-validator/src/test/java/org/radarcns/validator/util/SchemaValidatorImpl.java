package org.radarcns.validator.util;

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

import static org.radarcns.validator.util.SchemaValidator.validateTime;
import static org.radarcns.validator.util.SchemaValidator.validateTimeCompleted;
import static org.radarcns.validator.util.SchemaValidator.validateTimeReceived;

public final class SchemaValidatorImpl {

    public static final SchemaValidator ACTIVE_VALIDATION =
                validateTime().and(validateTimeCompleted());

    public static final SchemaValidator MONITOR_VALIDATION = validateTime();

    public static final SchemaValidator PASSIVE_VALIDATION =
                validateTime().and(validateTimeReceived());

    private SchemaValidatorImpl() {
        //Private constructor
    }
}
