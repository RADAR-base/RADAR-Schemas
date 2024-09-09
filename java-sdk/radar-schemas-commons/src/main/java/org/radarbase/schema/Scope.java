package org.radarbase.schema;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public enum Scope {
    ACTIVE, KAFKA, CATALOGUE, MONITOR, PASSIVE, STREAM, CONNECTOR, PUSH;

    private final String lower;

    Scope() {
        this.lower = name().toLowerCase(Locale.ROOT);
    }

    public Path getPath(Path root) {
        Path path = root.resolve(lower);
        return Files.exists(path) ? path : null;
    }

    public String getLower() {
        return lower;
    }
}
