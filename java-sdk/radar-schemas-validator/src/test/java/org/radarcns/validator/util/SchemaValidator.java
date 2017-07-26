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

import static org.radarcns.validator.util.SchemaValidatorRole.getActiveValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getGeneralValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getMonitorValidator;
import static org.radarcns.validator.util.SchemaValidatorRole.getPassiveValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.radarcns.validator.StructureValidator.NameFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO.
 */
public final class SchemaValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);

    private static final Map<String, List<Schema>> COLLISIONS = new HashMap<>();

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
        Objects.requireNonNull(schema);
        Objects.requireNonNull(pathToSchema);
        Objects.requireNonNull(root);
        Objects.requireNonNull(subfolder);

        ValidationResult result;

        switch (root) {
            case ACTIVE:
                result = getActiveValidator(pathToSchema, root, subfolder).apply(schema);
                break;
            case MONITOR:
                result = getMonitorValidator(pathToSchema, root, subfolder).apply(schema);
                break;
            case KAFKA:
                result = getGeneralValidator(pathToSchema, root, subfolder).apply(schema);
                break;
            case PASSIVE:
                result = getPassiveValidator(pathToSchema, root, subfolder).apply(schema);
                break;
            default:
                LOGGER.warn("Applying general validation to {}", getPath(pathToSchema));
                result = getGeneralValidator(pathToSchema, root, subfolder).apply(schema);
                break;
        }

        computeCollision(schema);

        if (!result.isValid()) {
            LOGGER.error("{} is invalid.", getPath(pathToSchema));
        }

        return result;
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
            String subfolder, Set<String> skipRecordName, Set<String> skipFieldName) {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(pathToSchema);
        Objects.requireNonNull(root);
        Objects.requireNonNull(subfolder);

        ValidationResult result;

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
                result = getGeneralValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                break;
            case PASSIVE:
                result = getPassiveValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                break;
            default:
                LOGGER.warn("Applying general validation to {}", getPath(pathToSchema));
                result = getGeneralValidator(pathToSchema, root, subfolder, skipRecordName,
                        skipFieldName).apply(schema);
                break;
        }

        computeCollision(schema);

        if (!result.isValid()) {
            LOGGER.error("{} is invalid.", getPath(pathToSchema));
        }

        return result;
    }

    public static String getPath(Path path) {
        return path.toString().substring(path.toString().indexOf("/RADAR-Schemas/"));
    }

    private static void computeCollision(Schema schema) {
        if (!schema.getType().equals(Type.RECORD)) {
            return;
        }

        for (Field field : schema.getFields()) {
            List<Schema> list = COLLISIONS.get(field.name());
            if (schema == null) {
                COLLISIONS.put(field.name(), Collections.singletonList(schema));
            } else {
                list.add(schema);
            }
            computeCollision(field.schema());
        }
    }

    /**
     * TODO.
     */
    public static void analyseCollision() {
        COLLISIONS.entrySet().stream()
                  .filter(entry -> entry.getValue().size() > 1)
                  .forEach(entry -> {
                      String message = entry.getKey() + " appears in: \n";
                      for (Schema schema : entry.getValue()) {
                          message += "\t - " + schema.getFullName() + " as "
                                  + schema.getField(
                                        entry.getKey()).schema().getType().getName().toUpperCase();
                      }

                      LOGGER.warn(message, entry.getKey());
                  });
    }
}
