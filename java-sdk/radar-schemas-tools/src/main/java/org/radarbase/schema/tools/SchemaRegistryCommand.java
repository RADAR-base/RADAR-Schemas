package org.radarbase.schema.tools;

import static org.radarbase.schema.registration.TopicRegistrar.matchTopic;

import java.net.MalformedURLException;
import java.util.Optional;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.registration.SchemaRegistry;
import org.radarbase.schema.registration.SchemaRegistry.Compatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaRegistryCommand implements SubCommand {

    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryCommand.class);

    @Override
    public String getName() {
        return "register";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        String url = options.get("schemaRegistry");
        String apiKey = options.getString("api_key");
        String apiSecret = options.getString("api_secret");
        try {
            SchemaRegistry registration = createSchemaRegistry(url, apiKey, apiSecret);

            boolean forced = options.getBoolean("force");
            if (forced && !registration.putCompatibility(Compatibility.NONE)) {
                return 1;
            }
            Pattern pattern = matchTopic(
                    options.getString("topic"), options.getString("match"));

            boolean result = registerSchemas(app, registration, pattern);

            if (forced) {
                registration.putCompatibility(Compatibility.FULL);
            }
            return result ? 0 : 1;
        } catch (MalformedURLException ex) {
            logger.error("Schema registry URL {} is invalid: {}", url, ex.toString());
            return 1;
        } catch (IllegalStateException | InterruptedException ex) {
            logger.error("Cannot reach schema registry. Aborting");
            return 1;
        }
    }

    private static SchemaRegistry createSchemaRegistry(String url, String apiKey, String apiSecret)
            throws MalformedURLException, InterruptedException {
        SchemaRegistry registry;
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            logger.info("Initializing standard SchemaRegistration ...");
            registry = new SchemaRegistry(url);
        } else {
            logger.info("Initializing SchemaRegistration with authentication...");
            registry = new SchemaRegistry(url, apiKey, apiSecret);
        }

        registry.initialize();
        return registry;
    }

    private static boolean registerSchemas(CommandLineApp app, SchemaRegistry registration,
            Pattern pattern) {
        if (pattern == null) {
            return registration.registerSchemas(app.getCatalogue());
        } else {
            Optional<Boolean> didUpload = app.getCatalogue().getTopics()
                    .filter(t -> pattern.matcher(t.getName()).find())
                    .map(registration::registerSchema)
                    .reduce((a, b) -> a && b);

            if (didUpload.isPresent()) {
                return didUpload.get();
            } else {
                logger.error("Topic {} does not match a known topic."
                                + " Find the list of acceptable topics"
                                + " with the `radar-schemas-tools list` command. Aborting.",
                        pattern);
                return false;
            }
        }
    }

    @Override
    public void addParser(ArgumentParser parser) {
        parser.description("Register schemas in the schema registry.");
        parser.addArgument("-f", "--force")
                .help("force registering schema, even if it is incompatible")
                .action(Arguments.storeTrue());
        parser.addArgument("-t", "--topic")
                .help("register the schemas of one topic")
                .type(String.class);
        parser.addArgument("-m", "--match")
                .help("register the schemas of all topics matching the given regex"
                        + "; does not do anything if --topic is specified")
                .type(String.class);
        parser.addArgument("schemaRegistry")
                .help("schema registry URL");
        parser.addArgument("-u", "--api-key")
                .help("Client password to authorize with.");
        parser.addArgument("-p", "--api-secret")
                .help("Client key to authorize with.");
        SubCommand.addRootArgument(parser);
    }
}
