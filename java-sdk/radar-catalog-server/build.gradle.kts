plugins {
    application
    id("kotlin-convention")
}

description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation(libs.radar.jersey)
    implementation(project(":radar-schemas-core"))
    implementation(libs.radar.commons.kotlin)

    implementation(libs.argparse4j)

    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.serialization.kotlinx.json)
}

application {
    mainClass.set("org.radarbase.schema.service.SourceCatalogueServer")
}
