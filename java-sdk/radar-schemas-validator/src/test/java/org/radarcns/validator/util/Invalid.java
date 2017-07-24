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

import java.util.Optional;

public class Invalid implements ValidationResult {

    private final String reason;

    Invalid(String reason) {
        this.reason = reason;
    }

    public boolean isValid() {
        return false;
    }

    public Optional<String> getReason() {
        return Optional.of(reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Invalid)) {
            return false;
        }

        Invalid invalid = (Invalid) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder().append(
                      reason, invalid.reason).isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(
              17, 37).append(reason).toHashCode();
    }
}
