package org.radarcns.schema.validation.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

/**
 * TODO.
 */
class ConfigItem {

    /** Possible check status. */
    private enum CheckStatus {
        DISABLE, ENABLE;

        private String name;

        CheckStatus() {
            this.name = this.name().toLowerCase(Locale.ENGLISH);
        }

        public String getName() {
            return name;
        }
    }

    @JsonProperty("record_name_check")
    @SuppressWarnings("PMD.ImmutableField")
    private CheckStatus nameRecordCheck = CheckStatus.ENABLE;

    private final Set<String> fields = new HashSet<>();

    public ConfigItem() {
      // POJO initializer
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    boolean isNameRecordDisable() {
        return nameRecordCheck.getName().equals(CheckStatus.DISABLE.getName());
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    @JsonSetter("fields")
    public void setFields(Collection<String> fields) {
        if (!this.fields.isEmpty()) {
            this.fields.clear();
        }
        this.fields.addAll(fields);
    }

    /**
     * TODO.
     *
     * @return TODO
     */
    public Set<String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "ConfigItem{"
            + "nameRecordCheck=" + nameRecordCheck
            + ", fields=" + fields
            + '}';
    }
}
