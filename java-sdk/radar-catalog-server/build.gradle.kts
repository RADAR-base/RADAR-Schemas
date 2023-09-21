description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation(project(":radar-schemas-core"))

    implementation("net.sourceforge.argparse4j:argparse4j:${Versions.argparse}")

    testImplementation(platform("io.ktor:ktor-bom:${Versions.ktor}"))
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json")
}

application {
    mainClass.set("org.radarbase.schema.service.SourceCatalogueServer")
}
