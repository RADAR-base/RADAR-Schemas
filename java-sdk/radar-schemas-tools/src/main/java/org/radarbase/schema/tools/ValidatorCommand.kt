package org.radarbase.schema.tools

import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.schema.Scope
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import org.radarbase.schema.validation.SchemaValidator
import org.radarbase.schema.validation.ValidationException
import org.radarbase.schema.validation.config.ExcludeConfig
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.streams.asSequence

class ValidatorCommand : SubCommand {
    override val name: String = "validate"

    override fun execute(options: Namespace, app: CommandLineApp): Int {
        try {
            println()
            println("Validated topics:")
            app.catalogue.sources.asSequence()
                .flatMap { it.data.asSequence() }
                .flatMap { d ->
                    try {
                        d.getTopics(app.catalogue.schemaCatalogue).asSequence()
                    } catch (ex: Exception) {
                        throw IllegalArgumentException(ex)
                    }
                }
                .distinctBy { it.name }
                .sortedBy { it.name }
                .forEach { topic ->
                    println("- ${topic.name} [${topic.keySchema.fullName}: ${topic.valueSchema.fullName}]")
                }
            println()
        } catch (ex: Exception) {
            System.err.println("Failed to load all topics: " + ex.message)
            return 1
        }

        val scope = options.getString("scope")
            ?.let { Scope.valueOf(it) }

        return try {
            val config = loadConfig(app.root, options.getString("config"))
            val validator = SchemaValidator(app.root, config)

            var exceptionStream = Stream.empty<ValidationException>()
            if (options.getBoolean("full")) {
                exceptionStream = validator.analyseFiles(
                    scope,
                    app.catalogue.schemaCatalogue)
            }
            if (options.getBoolean("from_specification")) {
                exceptionStream = Stream.concat(
                    exceptionStream,
                    validator.analyseSourceCatalogue(scope, app.catalogue)).distinct()
            }

            resolveValidation(exceptionStream, validator,
                options.getBoolean("verbose"),
                options.getBoolean("quiet"))
        } catch (e: IOException) {
            System.err.println("Failed to load schemas: $e")
            1
        }
    }

    override fun addParser(parser: ArgumentParser) {
        parser.apply {
            description("Validate a set of specifications.")
            addArgument("-s", "--scope")
                .help("type of specifications to validate")
                .choices(*Scope.values())
            addArgument("-c", "--config")
                .help("configuration file to use")
            addArgument("-v", "--verbose")
                .help("verbose validation message")
                .action(Arguments.storeTrue())
            addArgument("-q", "--quiet")
                .help("only set exit code.")
                .action(Arguments.storeTrue())
            addArgument("-S", "--from-specification")
                .help("validation of all schemas referenced in specifications")
                .action(Arguments.storeTrue())
            addArgument("-f", "--full")
                .help("full validation of contents")
                .action(Arguments.storeTrue())
            addRootArgument()
        }
    }

    private fun resolveValidation(
        stream: Stream<ValidationException>,
        validator: SchemaValidator,
        verbose: Boolean,
        quiet: Boolean
    ): Int = when {
        !quiet -> {
            val result = SchemaValidator.format(stream)
            println(result)
            if (verbose) {
                println("Validated schemas:")
                validator.validatedSchemas.keys
                    .sorted()
                    .forEach { println(" - $it") }
                println()
            }
            if (result.isNotEmpty()) 1 else 0
        }
        stream.count() > 0 -> 1
        else -> 0
    }

    companion object {
        @Throws(IOException::class)
        private fun loadConfig(root: Path, configSubPath: String?): ExcludeConfig {
            val configPath = if (configSubPath != null) {
                if (configSubPath[0] == '/') {
                    Paths.get(configSubPath)
                } else {
                    root.resolve(configSubPath)
                }
            } else null
            return ExcludeConfig.load(configPath)
        }
    }
}
