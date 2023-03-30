description = "RADAR Schemas specification and validation tools."

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":radar-schemas-registration"))

    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("net.sourceforge.argparse4j:argparse4j:${Versions.argparse}")

    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j2}")
}

application {
    mainClass.set("org.radarbase.schema.tools.CommandLineApp")
}
