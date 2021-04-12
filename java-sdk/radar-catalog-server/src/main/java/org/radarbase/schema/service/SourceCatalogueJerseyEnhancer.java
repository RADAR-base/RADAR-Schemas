package org.radarbase.schema.service;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.jetbrains.annotations.NotNull;
import org.radarbase.jersey.auth.AuthConfig;
import org.radarbase.jersey.auth.MPConfig;
import org.radarbase.jersey.config.ConfigLoader.Enhancers;
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
        abstractBinder.bind(sourceCatalogue)
                .to(SourceCatalogue.class);
    }

    @Override
    public void enhanceResources(@NotNull ResourceConfig resourceConfig) {
        AuthConfig authConfig = new AuthConfig(
                new MPConfig(),
                "res_sourceCatalogue",
                null,
                null,
                null,
                null,
                null,
                null);

        resourceConfig.register(Enhancers.INSTANCE.radar(authConfig));
        resourceConfig.register(Enhancers.INSTANCE.getDisabledAuthorization());
        resourceConfig.register(Enhancers.INSTANCE.getGeneralException());
        resourceConfig.register(Enhancers.INSTANCE.getHttpException());
        resourceConfig.register(Enhancers.INSTANCE.getHealth());
    }

    @Override
    public void enhance(@NotNull AbstractBinder abstractBinder) {
        enhanceBinder(abstractBinder);
    }

    @Override
    public void enhance(@NotNull ResourceConfig resourceConfig) {
        enhanceResources(resourceConfig);
    }
}
