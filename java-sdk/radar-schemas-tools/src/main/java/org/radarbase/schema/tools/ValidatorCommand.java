package org.radarbase.schema.tools;

import static org.radarbase.schema.util.SchemaUtils.applyOrIllegalException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import kotlin.Pair;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.radarbase.schema.SchemaCatalogue;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.DataProducer;
import org.radarbase.schema.validation.SchemaValidator;
import org.radarbase.schema.validation.ValidationException;
import org.radarbase.schema.validation.config.ExcludeConfig;
import org.radarbase.schema.validation.rules.SchemaMetadata;

public class ValidatorCommand implements SubCommand {
    @Override
    public String getName() {
        return "validate";
    }

    @Override
    public int execute(Namespace options, CommandLineApp app) {
        try {
            ExcludeConfig config = loadConfig(app.getRoot(), options.getString("config"));

            try {
                System.out.println();
                System.out.println("Validated topics:");
                app.getCatalogue().getSources().stream()
                        .flatMap(s -> s.getData().stream())
                        .flatMap(applyOrIllegalException(d ->
                                d.getTopics(app.getCatalogue().getSchemaCatalogue())))
                        .map(t -> "- "
                                + t.getName()
                                + " [" + t.getKeySchema().getFullName() + ": "
                                + t.getValueSchema().getFullName() + "]")
                        .sorted()
                        .distinct()
                        .forEach(System.out::println);
                System.out.println();
            } catch (Exception ex) {
                System.err.println("Failed to load all topics: " + ex.getMessage());
                return 1;
            }

            String scopeString = options.getString("scope");
            Scope scope = scopeString != null ? Scope.valueOf(scopeString) : null;

            Stream<ValidationException> exceptionStream = Stream.empty();
            SchemaValidator validator = new SchemaValidator(app.getRoot(), config);

            if (options.getBoolean("full")) {
                exceptionStream = validator.analyseFiles(
                        scope,
                        app.getCatalogue().getSchemaCatalogue());
            }
            if (options.getBoolean("from_specification")) {
                exceptionStream = Stream.concat(
                        exceptionStream,
                        validator.analyseSourceCatalogue(scope, app.getCatalogue())).distinct();
            }

            return resolveValidation(exceptionStream, validator,
                    options.getBoolean("verbose"),
                    options.getBoolean("quiet"));
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
        parser.addArgument("-S", "--from-specification")
                .help("validation of all schemas referenced in specifications")
                .action(Arguments.storeTrue());
        parser.addArgument("-f", "--full")
                .help("full validation of contents")
                .action(Arguments.storeTrue());
        SubCommand.addRootArgument(parser);
    }

    private int resolveValidation(Stream<ValidationException> stream,
            SchemaValidator validator,
            boolean verbose,
            boolean quiet) {
        if (quiet) {
            if (stream.count() > 0) {
                return 1;
            }
        } else {
            String result = SchemaValidator.format(stream);

            System.out.println(result);
            if (verbose) {
                System.out.println("Validated schemas:");
                Set<String> names = new TreeSet<>(
                        validator.getValidatedSchemas().keySet());
                for (String name : names) {
                    System.out.println(" - " + name);
                }
                System.out.println();
            }
            if (!result.isEmpty()) {
                return 1;
            }
        }
        return 0;
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
