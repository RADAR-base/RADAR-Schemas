package org.radarcns.schema.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.active.ActiveSource;
import org.radarcns.schema.specification.connector.ConnectorSource;
import org.radarcns.schema.specification.monitor.MonitorSource;
import org.radarcns.schema.specification.passive.PassiveSource;

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
    public SourceTypeResponse getPassiveSources() {
        return new SourceTypeResponse().addPassive();
    }

    /** Get all passive sources from the source catalogue. */
    @GET
    @Path("/active")
    public SourceTypeResponse getActiveSources() {
        return new SourceTypeResponse().addActive();
    }

    /** Get all monitor sources from the source catalogue. */
    @GET
    @Path("/monitor")
    public SourceTypeResponse getMonitorSources() {
        return new SourceTypeResponse().addMonitor();
    }


    /** Get all connector sources from the source catalogue. */
    @GET
    @Path("/connector")
    public SourceTypeResponse getConnectorSources() {
        return new SourceTypeResponse().addConnector();
    }

    /** Get all sources from the source catalogue. */
    @GET
    public SourceTypeResponse getAllSourceTypes() {
        return new SourceTypeResponse()
                .addPassive()
                .addActive()
                .addMonitor()
                .addConnector();
    }

    /** Response with source types. */
    public class SourceTypeResponse {
        @JsonProperty("passive-source-types")
        private List<PassiveSource> passiveSources;

        @JsonProperty("active-source-types")
        private List<ActiveSource> activeSources;

        @JsonProperty("monitor-source-types")
        private List<MonitorSource> monitorSources;

        @JsonProperty("connector-source-types")
        private List<ConnectorSource> connectorSources;

        private SourceTypeResponse addPassive() {
            this.passiveSources = new ArrayList<>(sourceCatalogue.getPassiveSources().values());
            return this;
        }

        private SourceTypeResponse addActive() {
            this.activeSources = new ArrayList<>(sourceCatalogue.getActiveSources().values());
            return this;
        }

        private SourceTypeResponse addMonitor() {
            this.monitorSources = new ArrayList<>(sourceCatalogue.getMonitorSources().values());
            return this;
        }

        private SourceTypeResponse addConnector() {
            this.connectorSources = new ArrayList<>(sourceCatalogue.getConnectorSources().values());
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

        public List<ConnectorSource> getConnectorSources() {
            return connectorSources;
        }
    }
}
