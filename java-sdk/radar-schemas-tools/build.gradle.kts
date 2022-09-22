description = "RADAR Schemas specification and validation tools."

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":radar-schemas-registration"))
    val jacksonVersion: String by project
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    val argparseVersion: String by project
    implementation("net.sourceforge.argparse4j:argparse4j:$argparseVersion")

    val log4j2Version: String by project
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")
}

application {
    mainClass.set("org.radarbase.schema.tools.CommandLineApp")
}
