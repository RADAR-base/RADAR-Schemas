plugins {
    // TODO Remove this when new release of radar-commons is available and used in this project.
    // This version has Sentry support built in for radar-kotlin plugin.
    id("io.sentry.jvm.gradle") version "4.11.0"
}

description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation(project(":radar-schemas-core"))
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("net.sourceforge.argparse4j:argparse4j:${Versions.argparse}")

    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json")
}

application {
    mainClass.set("org.radarbase.schema.service.SourceCatalogueServer")
}
