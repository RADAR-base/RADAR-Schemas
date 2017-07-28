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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * Checks that the tree folder structure respects the following structure
 * <ul>
 *   <li>commons</li>
 *    <ul>
 *      <li>active</li>
 *      <li>kafka</li>
 *      <li>monitor</li>
 *      <li>passive</li>
 *    </ul>
 *   <li>rest</li>
 *   <li>specification</li>
 *    <ul>
 *      <li>active</li>
 *      <li>monitor</li>
 *      <li>passive</li>
 *    </ul>
 * </ul>
 * At moment, the {@code restapi} does not have a well defined structure.
 * TODO.
 */
public class CatalogValidator {

    /** Folder names. */
    public enum NameFolder {
        ACTIVE("active"),
        COMMONS("commons"),
        KAFKA("kafka"),
        MONITOR("monitor"),
        PASSIVE("passive"),
        RESTAPI("restapi"),
        SPECIFICATION("specification");

        private final String name;

        NameFolder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /** Root paths. */
    public enum RootPath {
        COMMONS(Paths.get(
                  new File(".").toURI()).getParent().getParent().getParent().resolve(
                      NameFolder.COMMONS.getName())),
        REST_API(Paths.get(
                  new File(".").toURI()).getParent().getParent().getParent().resolve(
                      NameFolder.RESTAPI.getName())),
        SPECIFICATION(Paths.get(
                  new File(".").toURI()).getParent().getParent().getParent().resolve(
                      NameFolder.SPECIFICATION.getName()));

        private final Path path;

        RootPath(Path path) {
            this.path = path;
        }

        public Path getPath() {
            return path;
        }
    }

    /** Root folders. */
    public enum RootFolder {
        COMMONS(new File(RootPath.COMMONS.getPath().toUri())),
        REST_API(new File(RootPath.REST_API.getPath().toUri())),
        SPECIFICATION(new File(RootPath.SPECIFICATION.getPath().toUri()));

        private final File folder;

        RootFolder(File folder) {
            this.folder = folder;
        }

        public File getFolder() {
            return folder;
        }
    }

    /** Commons folders. */
    public enum CommonsFolder {
        ACTIVE(new File(RootPath.COMMONS.getPath().resolve(NameFolder.ACTIVE.getName()).toUri())),
        KAFKA(new File(RootPath.COMMONS.getPath().resolve(NameFolder.KAFKA.getName()).toUri())),
        MONITOR(new File(RootPath.COMMONS.getPath().resolve(NameFolder.MONITOR.getName()).toUri())),
        PASSIVE(new File(RootPath.COMMONS.getPath().resolve(NameFolder.PASSIVE.getName()).toUri()));

        private final File folder;

        CommonsFolder(File folder) {
            this.folder = folder;
        }

        public File getFolder() {
            return folder;
        }
    }

    /** Commons folders. */
    public enum SpecificationFolder {
        ACTIVE(new File(RootPath.SPECIFICATION.getPath().resolve(
              NameFolder.ACTIVE.getName()).toUri())),
        MONITOR(new File(RootPath.SPECIFICATION.getPath().resolve(
              NameFolder.MONITOR.getName()).toUri())),
        PASSIVE(new File(RootPath.SPECIFICATION.getPath().resolve(
              NameFolder.PASSIVE.getName()).toUri()));

        private final File folder;

        SpecificationFolder(File folder) {
            this.folder = folder;
        }

        public File getFolder() {
            return folder;
        }
    }

    @Test
    public void commons() throws IOException {
        assertEquals(true, RootFolder.COMMONS.getFolder().isDirectory());

        for (CommonsFolder folder : CommonsFolder.values()) {
            assertEquals(true, folder.getFolder().isDirectory());
        }

        CommonsValidator.validateAll();
    }

    @Test
    public void restapi() {
        assertEquals(true, RootFolder.REST_API.getFolder().isDirectory());

        //TODO check whether it is possible to define a structure
    }

    /*@Test
    public void specification() {
        assertEquals(true, RootFolder.SPECIFICATION.getFolder().isDirectory());

        for (SpecificationFolder folder : SpecificationFolder.values()) {
            assertEquals(true, folder.getFolder().isDirectory());
        }
    }*/
}
