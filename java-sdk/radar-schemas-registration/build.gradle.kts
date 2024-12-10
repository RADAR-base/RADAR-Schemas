plugins {
    id("java-library")
    id("publishing-convention")
}

description = "RADAR Schemas specification and validation tools"

dependencies {
    api(project(":radar-schemas-commons"))
    api(project(":radar-schemas-core"))

    implementation(libs.radar.commons)
    api(libs.radar.commons.server)
    implementation(libs.radar.commons.kotlin)

    implementation(libs.kafka.connect.avro.converter)
    implementation(libs.kafka.schema.registry.client)

    implementation(libs.connect.json)
    implementation(libs.ktor.client.auth)
    testImplementation(libs.mockwebserver)
}
