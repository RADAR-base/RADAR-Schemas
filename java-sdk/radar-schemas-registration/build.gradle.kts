description = "RADAR Schemas specification and validation tools"

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    api(project(":radar-schemas-commons"))
    api(project(":radar-schemas-core"))

    api("com.squareup.okhttp3:okhttp:${Versions.okHttp}")
    api("org.radarbase:radar-commons-server:${Versions.radarCommons}")
    api("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("io.confluent:kafka-connect-avro-converter:${Versions.confluent}")
    implementation("io.confluent:kafka-schema-registry-client:${Versions.confluent}")

    implementation("org.apache.kafka:connect-json:${Versions.kafka}")
    implementation("io.ktor:ktor-client-auth:2.2.4")
}
