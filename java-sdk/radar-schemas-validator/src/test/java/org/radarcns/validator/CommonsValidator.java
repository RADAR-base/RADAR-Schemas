package org.radarcns.validator;

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
import org.radarcns.validator.CatalogValidator.CommonsFolder;
import org.radarcns.validator.CatalogValidator.NameFolder;
import org.radarcns.validator.util.AvroValidator;

/**
 * TODO.
 */
public final class CommonsValidator {

    private CommonsValidator() {
        //Static class
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void validateAll() throws IOException {
        active();
        kafka();
        monitor();
        passive();

        AvroValidator.analyseNamingCollision(true);
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void active() throws IOException {
        AvroValidator.analiseFiles(CommonsFolder.ACTIVE.getFolder(), NameFolder.ACTIVE,
                null);
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void kafka() throws IOException {
        AvroValidator.analiseFiles(CommonsFolder.KAFKA.getFolder(), NameFolder.KAFKA,
                null);
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void monitor() throws IOException {
        AvroValidator.analiseFiles(CommonsFolder.MONITOR.getFolder(), NameFolder.MONITOR,
                null);
    }

    /**
     * TODO.
     * @throws IOException TODO
     */
    public static void passive() throws IOException {
        AvroValidator.analiseFiles(CommonsFolder.PASSIVE.getFolder(), NameFolder.PASSIVE,
                null);
    }
}
