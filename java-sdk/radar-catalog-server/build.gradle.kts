description = "RADAR Schemas specification and validation tools."

dependencies {
    val radarJerseyVersion: String by project
    implementation("org.radarbase:radar-jersey:$radarJerseyVersion")
    implementation(project(":radar-schemas-core"))

    val argparseVersion: String by project
    implementation("net.sourceforge.argparse4j:argparse4j:$argparseVersion")

    val log4j2Version: String by project
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
    runtimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")

    val okHttpVersion: String by project
    testImplementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
}

application {
    mainClass.set("org.radarbase.schema.service.SourceCatalogueServer")
}
