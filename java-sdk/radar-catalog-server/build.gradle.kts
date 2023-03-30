description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation(project(":radar-schemas-core"))

    implementation("net.sourceforge.argparse4j:argparse4j:${Versions.argparse}")

    testImplementation("com.squareup.okhttp3:okhttp:${Versions.okHttp}")
}

application {
    mainClass.set("org.radarbase.schema.service.SourceCatalogueServer")
}
