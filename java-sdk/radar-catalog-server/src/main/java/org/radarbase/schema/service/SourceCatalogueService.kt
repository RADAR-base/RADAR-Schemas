package org.radarbase.schema.service

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.schema.specification.SourceCatalogue

/**
 * Webservice resource to share SourceCatalogues. The response has a "Source-Type-Class" header that
 * mentions the class of SourceCatalogues and the body has the SourceCatalogues in JSON format.
 */
@Path("/source-types")
@Produces(MediaType.APPLICATION_JSON)
class SourceCatalogueService(
    @Context private val sourceCatalogue: SourceCatalogue,
) {
    /** Get all passive sources from the source catalogue.  */
    @get:Path("/passive")
    @get:GET
    val passiveSources: SourceTypeResponse
        get() = SourceTypeResponse(passiveSources = sourceCatalogue.passiveSources)

    /** Get all passive sources from the source catalogue.  */
    @get:Path("/active")
    @get:GET
    val activeSources: SourceTypeResponse
        get() = SourceTypeResponse(activeSources = sourceCatalogue.activeSources)

    /** Get all monitor sources from the source catalogue.  */
    @get:Path("/monitor")
    @get:GET
    val monitorSources: SourceTypeResponse
        get() = SourceTypeResponse(monitorSources = sourceCatalogue.monitorSources)

    /** Get all connector sources from the source catalogue.  */
    @get:Path("/connector")
    @get:GET
    val connectorSources: SourceTypeResponse
        get() = SourceTypeResponse(connectorSources = sourceCatalogue.connectorSources)

    /** Get all sources from the source catalogue.  */
    @get:GET
    val allSourceTypes: SourceTypeResponse
        get() = SourceTypeResponse(
            passiveSources = sourceCatalogue.passiveSources,
            activeSources = sourceCatalogue.activeSources,
            monitorSources = sourceCatalogue.monitorSources,
            connectorSources = sourceCatalogue.connectorSources,
        )
}
