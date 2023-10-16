package org.radarbase.schema.tools

import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.kotlin.coroutines.forkJoin
import org.radarbase.schema.registration.SchemaRegistry
import org.radarbase.schema.registration.TopicRegistrar
import org.radarbase.schema.specification.config.ToolConfig
import org.radarbase.schema.tools.SubCommand.Companion.addRootArgument
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.util.regex.Pattern

class SchemaRegistryCommand : SubCommand {
    override val name = "register"

    override suspend fun execute(options: Namespace, app: CommandLineApp): Int {
        val url = options.get<String>("schemaRegistry")
        val apiKey = options.getString("api_key")
            ?: System.getenv("SCHEMA_REGISTRY_API_KEY")
        val apiSecret = options.getString("api_secret")
            ?: System.getenv("SCHEMA_REGISTRY_API_SECRET")
        val toolConfigFile = options.getString("config")
        return try {
            val registration = SchemaRegistry(url, apiKey, apiSecret, app.config)
            val forced = options.getBoolean("force")
            if (forced && !registration.putCompatibility(SchemaRegistry.Compatibility.NONE)) {
                return 1
            }
            val pattern: Pattern? = TopicRegistrar.matchTopic(
                options.getString("topic"),
                options.getString("match"),
            )
            val result = registerSchemas(app, registration, pattern)
            if (forced) {
                registration.putCompatibility(SchemaRegistry.Compatibility.FULL)
            }
            if (result) 0 else 1
        } catch (ex: MalformedURLException) {
            logger.error(
                "Schema registry URL {} is invalid: {}",
                toolConfigFile,
                ex.toString(),
            )
            1
        } catch (ex: IOException) {
            logger.error("Topic configuration file {} is invalid: {}", url, ex.toString())
            1
        } catch (ex: IllegalStateException) {
            logger.error("Cannot reach schema registry. Aborting")
            1
        }
    }

    override fun addParser(parser: ArgumentParser) {
        parser.apply {
            description("Register schemas in the schema registry.")
            addArgument("-f", "--force")
                .help("force registering schema, even if it is incompatible")
                .action(Arguments.storeTrue())
            addArgument("-t", "--topic")
                .help("register the schemas of one topic")
                .type(String::class.java)
            addArgument("-m", "--match")
                .help(
                    "register the schemas of all topics matching the given regex" +
                        "; does not do anything if --topic is specified",
                )
                .type(String::class.java)
            addArgument("schemaRegistry")
                .help("schema registry URL")
            addArgument("-u", "--api-key")
                .help("Client password to authorize with.")
            addArgument("-p", "--api-secret")
                .help("Client key to authorize with.")
            addRootArgument()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(
            SchemaRegistryCommand::class.java,
        )

        @Throws(MalformedURLException::class, InterruptedException::class)
        private suspend fun SchemaRegistry(
            url: String,
            apiKey: String?,
            apiSecret: String?,
            toolConfig: ToolConfig,
        ): SchemaRegistry {
            val registry: SchemaRegistry = if (apiKey.isNullOrBlank() || apiSecret.isNullOrBlank()) {
                logger.info("Initializing standard SchemaRegistration ...")
                SchemaRegistry(url)
            } else {
                logger.info("Initializing SchemaRegistration with authentication...")
                SchemaRegistry(
                    url,
                    apiKey,
                    apiSecret,
                    toolConfig.topics,
                )
            }
            registry.initialize()
            return registry
        }

        private suspend fun registerSchemas(
            app: CommandLineApp,
            registration: SchemaRegistry,
            pattern: Pattern?,
        ): Boolean {
            return if (pattern == null) {
                registration.registerSchemas(app.catalogue)
            } else {
                app.catalogue.topics
                    .filter { pattern.matcher(it.name).find() }
                    .toList()
                    .forkJoin { registration.registerSchema(it) }
                    .reduceOrNull { a, b -> a && b }
                    ?: run {
                        logger.error(
                            "Topic {} does not match a known topic." +
                                " Find the list of acceptable topics" +
                                " with the `radar-schemas-tools list` command. Aborting.",
                            pattern,
                        )
                        false
                    }
            }
        }
    }
}
