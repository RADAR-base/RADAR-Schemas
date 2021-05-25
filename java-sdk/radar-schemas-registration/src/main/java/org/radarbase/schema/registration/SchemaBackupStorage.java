package org.radarbase.schema.registration;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Storage for _schemas topic backups.
 */
public interface SchemaBackupStorage {

    /**
     * Store a valid _schemas topic backup.
     *
     * @param topic backup to store.
     * @throws IOException if the data cannot be stored.
     */
    void store(SchemaTopicBackup topic) throws IOException;

    /**
     * Store an invalid _schemas topic backup.
     *
     * @param topic backup to store.
     * @throws IOException if the data cannot be stored.
     */
    void storeInvalid(SchemaTopicBackup topic) throws IOException;

    /**
     * Load a valid _schemas topic backup from storage.
     *
     * @return backup or {@code null} if no backup was available.
     * @throws IOException if the data cannot be stored.
     */
    SchemaTopicBackup load() throws IOException;

    Path getPath();
}
