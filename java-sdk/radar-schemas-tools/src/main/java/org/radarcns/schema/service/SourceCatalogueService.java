package org.radarcns.schema.service;

import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.passive.PassiveSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SourceCatalogueService {

    private final SourceCatalogue sourceCatalogue;

    public SourceCatalogueService(SourceCatalogue sourceCatalogue) {
        this.sourceCatalogue = sourceCatalogue;
    }

    @GET
    @Path("passive-sources")
    public List<PassiveSource> getPassiveSources() {
        List<PassiveSource> result = new ArrayList<>(this.sourceCatalogue.getPassiveSources().values());
        return  result;
    }
}
