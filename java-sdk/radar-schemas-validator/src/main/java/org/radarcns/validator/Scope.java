package org.radarcns.validator;

import java.nio.file.Files;
import java.nio.file.Path;

public enum Scope {
    ACTIVE("active"),
    KAFKA("kafka"),
    CATALOGUE("catalogue"),
    MONITOR("monitor"),
    PASSIVE("passive");

    private final Path specificationsPath;
    private final Path commonsPath;
    private final String name;

    Scope(String name) {
        this.name = name;
        Path newSpecificationsPath = SchemaRepository.SPECIFICATIONS_PATH.resolve(name);
        if (Files.exists(newSpecificationsPath)) {
            specificationsPath = newSpecificationsPath;
        } else {
            specificationsPath = null;
        }
        commonsPath = SchemaRepository.COMMONS_PATH.resolve(name);

    }

    public Path getSpecificationsPath() {
        return specificationsPath;
    }

    public Path getCommonsPath() {
        return commonsPath;
    }

    public String getName() {
        return name;
    }
}
