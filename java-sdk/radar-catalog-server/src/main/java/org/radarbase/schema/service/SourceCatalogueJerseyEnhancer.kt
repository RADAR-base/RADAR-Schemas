package org.radarbase.schema.service

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters.logResponse
import org.radarbase.schema.specification.SourceCatalogue

class SourceCatalogueJerseyEnhancer(
    private val sourceCatalogue: SourceCatalogue,
) : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
        logResponse,
        SourceCatalogueService::class.java,
    )

    override val packages: Array<String> = emptyArray()

    override fun AbstractBinder.enhance() {
        bindFactory { sourceCatalogue }
            .to(SourceCatalogue::class.java)
            .`in`(Singleton::class.java)
    }
}
