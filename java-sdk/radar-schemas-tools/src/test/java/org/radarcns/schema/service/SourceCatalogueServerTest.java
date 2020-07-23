package org.radarcns.schema.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.radarcns.schema.specification.SourceCatalogue;

public class SourceCatalogueServerTest {
    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();
    private SourceCatalogueServer server;
    private Thread serverThread;

    @Before
    public void setUp() {
        server = new SourceCatalogueServer(9876);
        serverThread = new Thread(() -> {
            try {
                SourceCatalogue sourceCatalog = SourceCatalogue.load(Paths.get("../.."));
                server.start(sourceCatalog);
                server.close();
            } catch (InterruptedException | IllegalStateException e) {
                // this is acceptable
            } catch (Exception e) {
                errorCollector.addError(e);
            }
        });
        serverThread.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        serverThread.join();
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
