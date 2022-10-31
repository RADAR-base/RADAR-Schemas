package org.radarbase.schema.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.radarbase.schema.specification.SourceCatalogue;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceCatalogueServerTest {
    private SourceCatalogueServer server;
    private Thread serverThread;
    private Exception exception;

    @BeforeEach
    public void setUp() {
        exception = null;
        server = new SourceCatalogueServer(9876);
        serverThread = new Thread(() -> {
            try {
                SourceCatalogue sourceCatalog = SourceCatalogue.Companion.load(Paths.get("../.."));
                server.start(sourceCatalog);
            } catch (IllegalStateException e) {
                // this is acceptable
            } catch (Exception e) {
                exception = e;
            }
        });
        serverThread.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        serverThread.interrupt();
        server.close();
        serverThread.join();
        if (exception != null) {
            throw exception;
        }
    }

    @Test
    public void sourceTypesTest() throws IOException, InterruptedException {
        Thread.sleep(5000L);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:9876/source-types")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertTrue(response.isSuccessful());
            ResponseBody body = response.body();
            assertNotNull(body);
            JsonNode node = new ObjectMapper().readTree(body.byteStream());
            assertTrue(node.isObject());
            assertTrue(node.has("passive-source-types"));
            assertTrue(node.get("passive-source-types").isArray());
        }
    }
}
