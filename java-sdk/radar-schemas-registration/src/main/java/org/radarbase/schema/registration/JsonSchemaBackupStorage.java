package org.radarbase.schema.registration;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Locale;
import javax.validation.constraints.NotNull;

/**
 * Schema topic backup storage to JSON files.
 */
public class JsonSchemaBackupStorage implements SchemaBackupStorage {
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaBackupStorage.class);

    private static final String EXT = ".json";
    private static final String INVALID_EXT = ".invalid" + EXT;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(Include.NON_NULL);
    private final Path path;

    public JsonSchemaBackupStorage(@NotNull Path path) {
        this.path = path.toAbsolutePath();
    }

    static boolean contentEquals(@NotNull Path path, @NotNull Path backupPath) throws IOException {
        if (Files.size(path) != Files.size(backupPath)) {
            return false;
        }

        try (InputStream input1 = Files.newInputStream(path);
                InputStream input2 = Files.newInputStream(backupPath)) {

            byte[] buf1 = new byte[4096];
            byte[] buf2 = new byte[4096];

            int numRead1 = input1.read(buf1);

            while (numRead1 != -1) {
                int numRead2 = input2.readNBytes(buf2, 0, numRead1);
                if (numRead2 < numRead1
                        || !Arrays.equals(buf1, 0, numRead1, buf2, 0, numRead1)) {
                    return false;
                }
                numRead1 = input1.read(buf1);
            }

            return input2.read() == -1;
        }
    }

    @Override
    public void store(SchemaTopicBackup topic) throws IOException {
        Path tmpPath = Files.createTempFile(path.getParent(), ".schema-backup", EXT);

        try (Writer writer = Files.newBufferedWriter(tmpPath)) {
            MAPPER.writeValue(writer, topic);
        }

        replaceAndBackup(tmpPath, path, EXT);
    }

    private void replaceAndBackup(Path tmpPath, Path mainPath, String suffix) throws IOException {
        if (!Files.exists(mainPath)) {
            logger.info("Creating new {}", mainPath);
            Files.move(tmpPath, mainPath, ATOMIC_MOVE);
        } else if (contentEquals(mainPath, tmpPath)) {
            logger.info("Not replacing old identical value {}", mainPath);
            Files.delete(tmpPath);
        } else {
            FileTime lastModified = Files.getLastModifiedTime(mainPath);
            Path backupPath = changeJsonSuffix(mainPath, "." + lastModified.toInstant() + suffix);
            logger.info("Creating new {} and moving the existing value to {}", mainPath, backupPath);
            Files.copy(mainPath, backupPath);
            Files.move(tmpPath, mainPath, ATOMIC_MOVE);
        }
    }

    @Override
    public void storeInvalid(@NotNull SchemaTopicBackup topic) throws IOException {
        Path tmpPath = Files.createTempFile(path.getParent(), ".schema-backup", INVALID_EXT);

        try (Writer writer = Files.newBufferedWriter(tmpPath)) {
            MAPPER.writeValue(writer, topic);
        }

        replaceAndBackup(tmpPath, changeJsonSuffix(path, INVALID_EXT), INVALID_EXT);
    }

    private Path changeJsonSuffix(@NotNull Path path, @NotNull String suffix) {
        String filename = path.getFileName().toString();
        if (filename.toLowerCase(Locale.US).endsWith(EXT)) {
            filename = filename.substring(0, filename.length() - EXT.length());
        }
        return path.getParent().resolve(filename + suffix);
    }

    @Override
    public SchemaTopicBackup load() throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return MAPPER.readValue(reader, SchemaTopicBackup.class);
        }
    }

    @Override
    public Path getPath() {
        return path;
    }
}
