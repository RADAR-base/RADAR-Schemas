package org.radarcns.schema.service;

import java.io.Closeable;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.util.SubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This server provides a webservice to share the SourceType Catalogues provided in *.yml files as
 * {@link org.radarcns.schema.service.SourceCatalogueService.SourceTypeResponse}
 */
public class SourceCatalogueServer implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogueServer.class);

    private final Server server;

    public SourceCatalogueServer(int serverPort) {
        this.server = new Server(serverPort);
    }

    @Override
    public void close() {
        try {
            this.server.join();
        } catch (InterruptedException e) {
            logger.error("Cannot stop server", e);
        }
        server.destroy();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private void start(SourceCatalogue sourceCatalogue) throws Exception {

        ResourceConfig config = new ResourceConfig();
        config.register(new SourceCatalogueService(sourceCatalogue));
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");
        server.start();
    }

    public static SubCommand command() {
        return new SourceCatalogueServiceCommand();
    }

    private static class SourceCatalogueServiceCommand implements SubCommand {

        @Override
        public String getName() {
            return "serve";
        }

        @Override
        public int execute(Namespace options, CommandLineApp app) {
            int partitions = options.getInt("port");
            SourceCatalogueServer service = new SourceCatalogueServer(partitions);
            try {
                service.start(app.getCatalogue());
            } catch (Exception e) {
                logger.error("Cannot start server ", e);
                return 1;
            }
            service.close();
            return 0;
        }

        @Override
        public void addParser(ArgumentParser parser) {
            parser.description("A web service to share source-type catalogs");
            parser.addArgument("-p" ,"--port")
                    .help("Port number of the SourceCatalogue Server ")
                    .type(Integer.class)
                    .setDefault(9010);
            SubCommand.addRootArgument(parser);
        }
    }


}
