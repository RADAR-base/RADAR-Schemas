description = "RADAR Schemas specification and validation tools"

dependencies {
    api(project(":radar-schemas-commons"))
    api(project(":radar-schemas-core"))

    implementation("org.radarbase:radar-commons:${Versions.radarCommons}")
    api("org.radarbase:radar-commons-server:${Versions.radarCommons}")
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("io.confluent:kafka-connect-avro-converter:${Versions.confluent}")
    implementation("io.confluent:kafka-schema-registry-client:${Versions.confluent}")

    implementation("org.apache.kafka:connect-json:${Versions.kafka}")
    implementation("io.ktor:ktor-client-auth:${Versions.ktor}")
    testImplementation("com.squareup.okhttp3:mockwebserver:${Versions.okHttp}")
}
