plugins {
    application
    id("kotlin-convention")
    id("org.radarbase.radar-kotlin")
}

description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation(project(":radar-schemas-registration"))
    implementation(platform(libs.jackson.bom))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation(libs.radar.commons.kotlin)
    implementation(libs.log4j.core)
    implementation(libs.argparse4j)
}

application {
    mainClass.set("org.radarbase.schema.tools.CommandLineApp")
}

// Versions of many dependencies defined by radar-commons-kotlin.
radarKotlin {
    log4j2Version.set(libs.versions.log4j2)
    sentryEnabled.set(true)
    openTelemetryAgentEnabled.set(true)
}
