package org.radarcns.schema.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.monitor.MonitorSource;
import org.radarcns.schema.specification.passive.PassiveSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Webservice resource to share SourceCatalogues. The response has a "Source-Type-Class" header that
 * mentions the class of SourceCatalogues and the body has the SourceCatalogues in JSON format.
 */
@Path("/source-types")
@Produces(MediaType.APPLICATION_JSON)
public class SourceCatalogueService {

    private final SourceCatalogue sourceCatalogue;

    SourceCatalogueService(SourceCatalogue sourceCatalogue) {
        this.sourceCatalogue = sourceCatalogue;
    }

    /** Get all passive sources from the source catalogue. */
    @GET
    @Path("/passive")
    public Response getPassiveSources() {
        return Response.ok()
                .entity(new SourceTypeResponse(this.sourceCatalogue).addPassive())
                .build();
    }

    /** Get all passive sources from the source catalogue. */
    @GET
    @Path("/active")
    public Response getActiveSources() {
        return Response.ok()
                .entity(new SourceTypeResponse(this.sourceCatalogue).addActive())
                .build();
    }

    /** Get all monitor sources from the source catalogue. */
    @GET
    @Path("/monitor")
    public Response getMonitorSources() {
        return Response.ok()
                .entity(new SourceTypeResponse(this.sourceCatalogue).addMonitor())
                .build();
    }

    /** Get all sources from the source catalogue. */
    @GET
    public Response getAllSourceTypes() {
        return Response.ok()
                .entity(new SourceTypeResponse(this.sourceCatalogue)
                        .addPassive()
                        .addActive()
                        .addMonitor())
                .build();
    }

    /** Response with source types. */
    public class SourceTypeResponse {
        @JsonIgnore
        private final SourceCatalogue sourceCatalogue;

        @JsonProperty("addPassive-source-types")
        private List<PassiveSource> passiveSources;

        @JsonProperty("addActive-source-types")
        private List<ActiveSource> activeSources;

        @JsonProperty("addMonitor-source-types")
        private List<MonitorSource> monitorSources;

        private SourceTypeResponse(SourceCatalogue sourceCatalogue) {
            this.sourceCatalogue = sourceCatalogue;
        }

        private SourceTypeResponse addPassive() {
            this.passiveSources = new ArrayList<>(
                    this.sourceCatalogue.getPassiveSources().values());
            return this;
        }

        private SourceTypeResponse addActive() {
            this.activeSources = new ArrayList<>(this.sourceCatalogue.getActiveSources().values());
            return this;
        }

        private SourceTypeResponse addMonitor() {
            this.monitorSources = new ArrayList<>(
                    this.sourceCatalogue.getMonitorSources().values());
            return this;
        }

        public List<PassiveSource> getPassiveSources() {
            return passiveSources;
        }

        public List<ActiveSource> getActiveSources() {
            return activeSources;
        }

        public List<MonitorSource> getMonitorSources() {
            return monitorSources;
        }
    }
}
