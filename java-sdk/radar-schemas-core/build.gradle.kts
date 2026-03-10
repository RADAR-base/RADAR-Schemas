plugins {
    alias(libs.plugins.kotlin.allopen)
}

description = "RADAR Schemas core specification and validation tools."

dependencies {
    api(libs.apache.avro) {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
    api(libs.jakarta.validation)
    api(project(":radar-schemas-commons"))
    implementation(libs.radar.commons.kotlin)

    api(platform(libs.jackson.bom))
    api(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.yaml)

    implementation(libs.confluent.kafka.connect.avro.data) {
        exclude(group = "org.glassfish.jersey.core", module = "jersey-common")
        exclude(group = "jakarta.ws.rs", module = "jakarta.ws.rs-api")
        exclude(group = "io.swagger", module = "swagger-annotations")
        exclude(group = "io.confluent", module = "common-utils")
        exclude(group = "io.confluent", module = "kafka-schema-serializer")
    }

    implementation(libs.kafka.connect.api) {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
        exclude(group = "javax.ws.rs", module = "javax.ws.rs-api")
    }

    api(libs.okhttp)
    api(libs.radar.commons.server)
}

allOpen {
    annotation("org.radarbase.config.OpenConfig")
}
