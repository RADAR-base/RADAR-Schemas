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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO.
 */
public class SkipConfig {

    private boolean nameRecord;
    private Set<String> fields;

    /**
     * TODO.
     * @param nameRecord TODO
     */
    public SkipConfig(boolean nameRecord) {
        this(nameRecord, "");
    }

    /**
     * TODO.
     * @param fields TODO
     */
    public SkipConfig(String... fields) {
        this(false, fields);
    }

    /**
     * TODO.
     * @param nameRecord TODO
     * @param fields TODO
     */
    public SkipConfig(boolean nameRecord, String... fields) {
        this.nameRecord = nameRecord;
        this.fields = fields.length == 1 && fields[0].isEmpty() ? null :
                new HashSet<>(Arrays.asList(fields));
    }

    /**
     * TODO.
     * @return TODO
     */
    public boolean isNameRecord() {
        return nameRecord;
    }

    /**
     * TODO.
     * @return TODO
     */
    public Set<String> getFields() {
        return fields;
    }
}
