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

    private static final String SOURCE_TYPE_CLASS_HEADER = "Source-Type-Class";
    private final SourceCatalogue sourceCatalogue;

    SourceCatalogueService(SourceCatalogue sourceCatalogue) {
        this.sourceCatalogue = sourceCatalogue;
    }

    @GET
    @Path("/passive")
    public Response getPassiveSources() {
        return Response.ok().entity(new SourceTypeResponse(this.sourceCatalogue).passive())
                .header(SOURCE_TYPE_CLASS_HEADER, "PASSIVE").build();
    }

    @GET
    @Path("/active")
    public Response getActiveSources() {
        return Response.ok().entity(new SourceTypeResponse(this.sourceCatalogue).active())
                .header(SOURCE_TYPE_CLASS_HEADER, "ACTIVE").build();
    }

    @GET
    @Path("/monitor")
    public Response getMonitorSources() {
        return Response.ok().entity(new SourceTypeResponse(this.sourceCatalogue).monitor())
                .header(SOURCE_TYPE_CLASS_HEADER, "MONITOR").build();
    }

    @GET
    public Response getAllSourceTypes() {
        return Response.ok().entity(new SourceTypeResponse(this.sourceCatalogue).all())
                .header(SOURCE_TYPE_CLASS_HEADER, "ALL").build();
    }

    public class SourceTypeResponse {

        @JsonIgnore
        private final SourceCatalogue sourceCatalogue;

        @JsonProperty("passive-source-types")
        private List<PassiveSource> passiveSources;

        @JsonProperty("active-source-types")
        private List<ActiveSource> activeSources;

        @JsonProperty("monitor-source-types")
        private List<MonitorSource> monitorSources;

        private SourceTypeResponse(SourceCatalogue sourceCatalogue) {
            this.sourceCatalogue = sourceCatalogue;
        }

        private SourceTypeResponse passive() {
            this.passiveSources = new ArrayList<>(
                    this.sourceCatalogue.getPassiveSources().values());
            return this;
        }

        public SourceTypeResponse active() {
            this.activeSources = new ArrayList<>(this.sourceCatalogue.getActiveSources().values());
            return this;
        }

        public SourceTypeResponse monitor() {
            this.monitorSources = new ArrayList<>(
                    this.sourceCatalogue.getMonitorSources().values());
            return this;
        }

        private SourceTypeResponse all() {
            this.passiveSources = new ArrayList<>(
                    this.sourceCatalogue.getPassiveSources().values());
            this.activeSources = new ArrayList<>(this.sourceCatalogue.getActiveSources().values());
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
