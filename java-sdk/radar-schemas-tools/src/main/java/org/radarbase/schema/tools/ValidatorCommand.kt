package org.radarbase.schema.tools

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.schema.Scope
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import org.radarbase.schema.validation.SchemaValidator
import org.radarbase.schema.validation.ValidationException
import org.radarbase.schema.validation.ValidationHelper.COMMONS_PATH
import org.radarbase.schema.validation.toFormattedString
import java.io.IOException
import kotlin.streams.asSequence

class ValidatorCommand : SubCommand {
    override val name: String = "validate"

    override suspend fun execute(options: Namespace, app: CommandLineApp): Int {
        try {
            println()
            println("Validated topics:")
            app.catalogue.sources.asSequence()
                .flatMap { it.data.asSequence() }
                .flatMap { d ->
                    try {
                        d.topics(app.catalogue.schemaCatalogue).asSequence()
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
            val validator = SchemaValidator(app.root.resolve(COMMONS_PATH), app.config.schemas)

            coroutineScope {
                val fullValidationJob = async {
                    if (options.getBoolean("full")) {
                        if (scope == null) {
                            validator.analyseFiles(app.catalogue.schemaCatalogue)
                        } else {
                            validator.analyseFiles(app.catalogue.schemaCatalogue, scope)
                        }
                    } else {
                        emptyList()
                    }
                }
                val fromSpecJob = async {
                    if (options.getBoolean("from_specification")) {
                        validator.analyseSourceCatalogue(scope, app.catalogue)
                    } else {
                        emptyList()
                    }
                }
                val exceptions = fullValidationJob.await() + fromSpecJob.await()

                resolveValidation(
                    exceptions,
                    validator,
                    options.getBoolean("verbose"),
                    options.getBoolean("quiet"),
                )
            }
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
                .choices(Scope.entries)
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
        stream: List<ValidationException>,
        validator: SchemaValidator,
        verbose: Boolean,
        quiet: Boolean,
    ): Int = when {
        !quiet -> {
            val result = stream.toFormattedString()
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
        stream.isNotEmpty() -> 1
        else -> 0
    }
}
