package org.radarbase.schema.service;

import jakarta.inject.Singleton;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jetbrains.annotations.NotNull;
import org.radarbase.jersey.config.ConfigLoader.Filters;
import org.radarbase.jersey.config.JerseyResourceEnhancer;
import org.radarbase.schema.specification.SourceCatalogue;

public class SourceCatalogueJerseyEnhancer implements JerseyResourceEnhancer {
    private final SourceCatalogue sourceCatalogue;

    public SourceCatalogueJerseyEnhancer(SourceCatalogue sourceCatalogue) {
        this.sourceCatalogue = sourceCatalogue;
    }

    @NotNull
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            Filters.INSTANCE.getLogResponse(),
            SourceCatalogueService.class,
        };
    }

    @NotNull
    @Override
    public String[] getPackages() {
        return new String[0];
    }

    @Override
    public void enhanceBinder(
            @NotNull AbstractBinder abstractBinder) {
        enhance(abstractBinder);
    }

    @Override
    public void enhanceResources(@NotNull ResourceConfig resourceConfig) {
        enhance(resourceConfig);
    }

    @Override
    public void enhance(@NotNull AbstractBinder abstractBinder) {
        abstractBinder.bindFactory(() -> sourceCatalogue)
                .to(SourceCatalogue.class)
                .in(Singleton.class);
    }

    @Override
    public void enhance(@NotNull ResourceConfig resourceConfig) {
        // do nothing
    }
}
