package org.radarbase.schema.service

import kotlinx.coroutines.runBlocking
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader.createResourceConfig
import org.radarbase.jersey.enhancer.Enhancers.exception
import org.radarbase.jersey.enhancer.Enhancers.health
import org.radarbase.jersey.enhancer.Enhancers.mapper
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.ToolConfig
import org.radarbase.schema.specification.config.loadToolConfig
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * This server provides a webservice to share the SourceType Catalogues provided in *.yml files as
 * [org.radarbase.schema.service.SourceCatalogueService.SourceTypeResponse]
 */
class SourceCatalogueServer(
    private val serverPort: Int,
) : Closeable {
    private lateinit var server: GrizzlyServer

    fun start(sourceCatalogue: SourceCatalogue) {
        val config = createResourceConfig(
            listOf(
                mapper,
                exception,
                health,
                SourceCatalogueJerseyEnhancer(sourceCatalogue),
            ),
        )
        server = GrizzlyServer(URI.create("http://0.0.0.0:$serverPort/"), config, false)
        server.listen()
    }

    override fun close() {
        server.shutdown()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SourceCatalogueServer::class.java)

        @JvmStatic
        fun main(vararg args: String) {
            val logger = LoggerFactory.getLogger(SourceCatalogueServer::class.java)
            val parser = ArgumentParsers.newFor("radar-catalog-server")
                .addHelp(true)
                .build()
                .description("RADAR catalog server for source types")
            parser.addArgument("-p", "--port")
                .help("server port")
                .type(Int::class.java).default = 9010
            parser.addArgument("-c", "--config")
                .help("Configuration YAML")
                .type(String::class.java)
            parser.addArgument("root")
                .help("Root path of the source catalogue")

            val parsedArgs: Namespace = try {
                parser.parseArgs(args)
            } catch (e: HelpScreenException) {
                parser.printHelp()
                exitProcess(0)
            } catch (e: ArgumentParserException) {
                logger.error("Failed to parse arguments: {}", e.message)
                logger.error(parser.formatUsage())
                exitProcess(1)
            }
            val config = loadConfig(parsedArgs.getString("config"))
            val sourceCatalogue: SourceCatalogue = try {
                runBlocking {
                    SourceCatalogue(
                        Paths.get(parsedArgs.getString("root")),
                        schemaConfig = config.schemas,
                        sourceConfig = config.sources,
                    )
                }
            } catch (e: IOException) {
                logger.error("Failed to load source catalogue", e)
                logger.error(parser.formatUsage())
                exitProcess(1)
            }

            // Processing state cannot be imported by ManagementPortal at this time.
            sourceCatalogue.passiveSources.stream()
                .flatMap { s -> s.data.stream() }
                .forEach { d -> d.processingState = null }

            SourceCatalogueServer(parsedArgs.getInt("port")).use { server ->
                server.start(sourceCatalogue)
            }
        }

        private fun loadConfig(fileName: String): ToolConfig = try {
            loadToolConfig(fileName)
        } catch (ex: IOException) {
            logger.error(
                "Cannot configure radar-catalog-server from config file {}: {}",
                fileName,
                ex.message,
            )
            exitProcess(1)
        }
    }
}
