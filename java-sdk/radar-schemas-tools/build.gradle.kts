description = "RADAR Schemas specification and validation tools."

dependencies {
    implementation(project(":radar-schemas-registration"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j2}")

    implementation("net.sourceforge.argparse4j:argparse4j:${Versions.argparse}")
}

application {
    mainClass.set("org.radarbase.schema.tools.CommandLineApp")
}
