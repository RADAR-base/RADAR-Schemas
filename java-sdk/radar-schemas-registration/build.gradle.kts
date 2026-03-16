description = "RADAR Schemas specification and validation tools"

dependencies {
    api(project(":radar-schemas-commons"))
    api(project(":radar-schemas-core"))

    implementation(libs.radar.commons)
    api(libs.radar.commons.server)
    implementation(libs.radar.commons.kotlin)

    implementation(libs.confluent.kafka.connect.avro.converter)
    implementation(libs.confluent.kafka.schema.registry.client)

    implementation(libs.kafka.connect.json)
    implementation(libs.ktor.client.auth)
    testImplementation(libs.okhttp.mockwebserver)
}
