package org.radarbase.schema.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.Scope;
import org.radarbase.schema.validation.SchemaValidator;
import org.radarbase.schema.validation.ValidationException;
import org.radarbase.schema.validation.config.ExcludeConfig;

public class ValidatorCommand implements SubCommand {

    @Override
    public String getName() {
        return "validate";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        try {
            ExcludeConfig config = loadConfig(app.getRoot(), options.getString("config"));
            SchemaValidator validator = new SchemaValidator(app.getRoot(), config);
            Stream<ValidationException> stream = validateSchemas(
                    options.getString("scope"), validator);

            if (options.getBoolean("quiet")) {
                return stream.count() > 0 ? 1 : 0;
            } else {
                String result = SchemaValidator.format(stream);

                System.out.println(result);
                if (options.getBoolean("verbose")) {
                    System.out.println("Validated schemas:");
                    Set<String> names = new TreeSet<>(
                            validator.getValidatedSchemas().keySet());
                    for (String name : names) {
                        System.out.println(" - " + name);
                    }
                    System.out.println();
                }
                return result.isEmpty() ? 0 : 1;
            }
        } catch (IOException e) {
            System.err.println("Failed to load schemas: " + e);
            return 1;
        }
    }

    @Override
    public void addParser(ArgumentParser parser) {
        parser.description("Validate a set of specifications.");
        parser.addArgument("-s", "--scope")
                .help("type of specifications to validate")
                .choices(Scope.values());
        parser.addArgument("-c", "--config")
                .help("configuration file to use");
        parser.addArgument("-v", "--verbose")
                .help("verbose validation message")
                .action(Arguments.storeTrue());
        parser.addArgument("-q", "--quiet")
                .help("only set exit code.")
                .action(Arguments.storeTrue());
        SubCommand.addRootArgument(parser);
    }


    private Stream<ValidationException> validateSchemas(
            String scopeString,
            SchemaValidator validator) {
        if (scopeString == null) {
            return validator.analyseFiles();
        } else {
            return validator.analyseFiles(Scope.valueOf(scopeString));
        }
    }

    private ExcludeConfig loadConfig(Path root, String configSubPath) throws IOException {
        Path configPath;
        if (configSubPath != null) {
            if (configSubPath.charAt(0) == '/') {
                configPath = Paths.get(configSubPath);
            } else {
                configPath = root.resolve(configSubPath);
            }
        } else {
            configPath = null;
        }
        return ExcludeConfig.load(configPath);
    }
}
