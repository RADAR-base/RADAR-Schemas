package org.radarcns.schema;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public enum Scope {
    ACTIVE, KAFKA, CATALOGUE, MONITOR, PASSIVE, STREAM;

    private final String lower;

    Scope() {
        this.lower = name().toLowerCase(Locale.ENGLISH);
    }

    public Path getPath(Path root) {
        Path path = root.resolve(name().toLowerCase(Locale.ENGLISH));
        return Files.exists(path) ? path : null;
    }

    public String getLower() {
        return lower;
    }
}
