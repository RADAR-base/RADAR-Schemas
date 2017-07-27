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

import static org.radarcns.validator.util.SchemaValidatorRole.TIME;
import static org.radarcns.validator.util.SchemaValidatorRole.TIME_COMPLETED;
import static org.radarcns.validator.util.SchemaValidatorRole.TIME_RECEIVED;
import static org.radarcns.validator.util.SchemaValidatorRole.getActiveValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getGeneralEnumValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getGeneralRecordValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getMonitorValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getPassiveValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.radarcns.validator.CatalogValidator.NameFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
final class SchemaValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);

    private static final Map<String, List<Schema>> COLLISIONS = new HashMap<>();

    private static final Set<String> AVOID_COLLISION_CHECK = new HashSet<>();

    static {
        AVOID_COLLISION_CHECK.add(TIME);
        AVOID_COLLISION_CHECK.add(TIME_COMPLETED);
        AVOID_COLLISION_CHECK.add(TIME_RECEIVED);
    }

    private SchemaValidator() {
        //Static class
    }

    /**
     * TODO.
     * @param schema TODO
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @return TODO
     * @throws IOException TODO
     */
    public static ValidationResult validate(Schema schema, Path pathToSchema, NameFolder root,
            String subfolder) {
        return validate(schema, pathToSchema, root, subfolder,
                false, null);
    }

    /**
     * TODO.
     * @param schema TODO
     * @param pathToSchema TODO
     * @param root TODO
     * @param subfolder TODO
     * @param skipRecordName TODO
     * @param skipFieldName TODO
     * @return TODO
     * @throws IOException TODO
     */
    public static ValidationResult validate(Schema schema, Path pathToSchema, NameFolder root,
            String subfolder, boolean skipRecordName, Set<String> skipFieldName) {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(pathToSchema);
        Objects.requireNonNull(root);
        Objects.requireNonNull(subfolder);

        ValidationResult result;

        if (schema.getType().equals(Type.ENUM)) {
            result = getGeneralEnumValidator(pathToSchema, root, subfolder,
                    skipRecordName).apply(schema);
        } else {
            switch (root) {
                case ACTIVE:
                    result = getActiveValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                    break;
                case MONITOR:
                    result = getMonitorValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                    break;
                case KAFKA:
                    result = getGeneralRecordValidator(pathToSchema, root, subfolder,
                        skipRecordName, skipFieldName).apply(schema);
                    break;
                case PASSIVE:
                    result = getPassiveValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                    break;
                default:
                    LOGGER.warn("Applying general validation to {}", getPath(pathToSchema));
                    result = getGeneralRecordValidator(pathToSchema, root, subfolder,
                        skipRecordName, skipFieldName).apply(schema);
                    break;
            }
        }

        computeCollision(schema);

        return result;
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    public static String getPath(Path path) {
        return path.toString().substring(path.toString().indexOf("/RADAR-Schemas/"));
    }

    /**
     * TODO.
     * @param schema TODO.
     */
    private static void computeCollision(Schema schema) {
        if (!schema.getType().equals(Type.RECORD)) {
            return;
        }

        for (Field field : schema.getFields()) {

            if (AVOID_COLLISION_CHECK.contains(field.name())) {
                continue;
            }

            List<Schema> list = COLLISIONS.get(field.name());
            if (list == null) {
                list = new LinkedList<>();
                list.add(schema);
                COLLISIONS.put(field.name(), list);
            } else {
                list.add(schema);
            }
            computeCollision(field.schema());
        }
    }

    /**
     * TODO.
     * @return TODO
     */
    public static StringBuilder analyseCollision() {
        int capacity = 100 * COLLISIONS.values().stream().mapToInt(List::size).sum()
                + COLLISIONS.size() * 120 + 100;

        StringBuilder messageBuilder = new StringBuilder(capacity);
        COLLISIONS.entrySet().stream()
                  .filter(entry -> entry.getValue().size() > 1)
                  .forEach(entry -> {
                      messageBuilder.append(entry.getKey().concat(" appears in:\n"));
                      entry.getValue().stream().forEach(
                              schema -> messageBuilder.append("\t - "
                                                      .concat(schema.getFullName())
                                                      .concat(" as ")
                                                      .concat(schema.getField(entry.getKey())
                                                                    .schema()
                                                                    .getType()
                                                                    .getName()
                                                                    .toUpperCase())
                                                      .concat("\n")));

                      messageBuilder.append("In case they have different use-cases, please modify "
                              + "the name field accordingly.\n");
                  });

        return messageBuilder;
    }
}
