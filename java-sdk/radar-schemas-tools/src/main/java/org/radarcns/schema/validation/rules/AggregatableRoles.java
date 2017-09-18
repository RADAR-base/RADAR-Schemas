package org.radarcns.schema.validation.rules;

import org.radarcns.schema.util.Utils;
import org.radarcns.schema.specification.Aggregatable;
import org.radarcns.schema.validation.ValidationSupport;

import static org.radarcns.schema.validation.ValidationSupport.isValidClass;
import static org.radarcns.schema.validation.rules.Validator.validateNonNull;
import static org.radarcns.schema.validation.rules.Validator.validateOrNull;

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
public final class AggregatableRoles {
    private static final String AGGREGATOR_PACKAGE = Utils.getProjectGroup()
            + ValidationSupport.Package.AGGREGATOR.getName();

    private static final String AGGREGATOR = "Kafka aggregator class is invalid,"
            + " it should must be a valid AVRO "
            + "schema located at " + ValidationSupport.Package.AGGREGATOR.getName() + ".";

    private AggregatableRoles() {
        // utility class
    }

    /**
     * TODO.
     * @param nullable TODO
     * @return TODO
     */
    static Validator<Aggregatable> validateAggregator(boolean nullable) {
        if (nullable) {
            return validateOrNull(Aggregatable::getAggregator,
                    aggregator -> aggregator.startsWith(AGGREGATOR_PACKAGE)
                            && isValidClass(aggregator), AGGREGATOR);
        } else {
            return validateNonNull(Aggregatable::getAggregator,
                    aggregator -> aggregator.startsWith(AGGREGATOR_PACKAGE)
                            && isValidClass(aggregator), AGGREGATOR);
        }
    }
}
