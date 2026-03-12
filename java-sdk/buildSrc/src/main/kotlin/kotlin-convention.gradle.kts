plugins {
    id("org.radarbase.radar-kotlin")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// Versions of many dependencies defined by radar-commons-kotlin.
radarKotlin {
    log4j2Version.set(libs.findVersion("log4j2").get().toString())
    sentryEnabled.set(true)
    openTelemetryAgentEnabled.set(true)
}
