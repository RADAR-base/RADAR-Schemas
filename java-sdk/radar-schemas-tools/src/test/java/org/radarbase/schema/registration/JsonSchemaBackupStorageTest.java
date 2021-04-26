package org.radarbase.schema.registration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class JsonSchemaBackupStorageTest {

    @Test
    public void contentEquals() throws IOException {
        Path path1 = Files.createTempFile("some", "test");
        Path path2 = Files.createTempFile("some", "test");
        Path path3 = Files.createTempFile("some", "test");
        Path path4 = Files.createTempFile("some", "test");
        Files.writeString(path1, "some");
        Files.writeString(path2, "some");
        Files.writeString(path3, "soma");
        Files.writeString(path4, "somee");

        assertTrue(JsonSchemaBackupStorage.contentEquals(path1, path2));
        assertTrue(JsonSchemaBackupStorage.contentEquals(path1, path1));
        assertFalse(JsonSchemaBackupStorage.contentEquals(path1, path3));
        assertFalse(JsonSchemaBackupStorage.contentEquals(path1, path4));
        assertFalse(JsonSchemaBackupStorage.contentEquals(path4, path1));
    }
}
