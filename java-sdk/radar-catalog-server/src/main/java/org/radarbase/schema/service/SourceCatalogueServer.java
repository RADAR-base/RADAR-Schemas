package org.radarbase.schema.service;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.glassfish.jersey.server.ResourceConfig;
import org.radarbase.jersey.GrizzlyServer;
import org.radarbase.jersey.config.ConfigLoader;
import org.radarbase.schema.specification.SourceCatalogue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

/**
 * This server provides a webservice to share the SourceType Catalogues provided in *.yml files as
 * {@link org.radarbase.schema.service.SourceCatalogueService.SourceTypeResponse}
 */
public class SourceCatalogueServer implements Closeable {
    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    private GrizzlyServer server;
    private final int serverPort;

    public SourceCatalogueServer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void close() {
        if (server != null) {
            server.shutdown();
        }
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void start(SourceCatalogue sourceCatalogue) {
        ResourceConfig config = ConfigLoader.INSTANCE.createResourceConfig(List.of(
                ConfigLoader.Enhancers.INSTANCE.getMapper(),
                ConfigLoader.Enhancers.INSTANCE.getOkhttp(),
                ConfigLoader.Enhancers.INSTANCE.getGeneralException(),
                ConfigLoader.Enhancers.INSTANCE.getHttpException(),
                ConfigLoader.Enhancers.INSTANCE.getHealth(),
                new SourceCatalogueJerseyEnhancer(sourceCatalogue)));
        server = new GrizzlyServer(URI.create("http://0.0.0.0:" + serverPort + "/"), config, false);
        server.listen();
    }

    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(SourceCatalogueServer.class);

        ArgumentParser parser = ArgumentParsers.newFor("radar-catalog-server")
                .addHelp(true)
                .build()
                .description("RADAR catalog server for source types");

        parser.addArgument("-p", "--port")
                .help("server port")
                .type(Integer.class)
                .setDefault(9010);

        parser.addArgument("root")
                .help("Root path of the source catalogue");

        Namespace parsedArgs;
        try {
            parsedArgs = parser.parseArgs(args);
        } catch (HelpScreenException e) {
            parser.printHelp();
            System.exit(0);
            return;
        } catch (ArgumentParserException e) {
            logger.error("Failed to parse arguments: {}", e.getMessage());
            logger.error(parser.formatUsage());
            System.exit(1);
            return;
        }

        SourceCatalogue sourceCatalogue;
        try {
            sourceCatalogue = SourceCatalogue
                    .load(Paths.get(parsedArgs.getString("root")));
        } catch (IOException e) {
            logger.error("Failed to load source catalogue", e);
            logger.error(parser.formatUsage());
            System.exit(1);
            return;
        }

        // Processing state cannot be imported by ManagementPortal at this time.
        sourceCatalogue.getPassiveSources().values().stream()
                .flatMap(s -> s.getData().stream())
                .forEach(d -> d.setProcessingState(null));

        try (SourceCatalogueServer server = new SourceCatalogueServer(parsedArgs.getInt("port"))) {
            server.start(sourceCatalogue);
        }
    }
}
