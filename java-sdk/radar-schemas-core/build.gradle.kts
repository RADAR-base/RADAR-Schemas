plugins {
    kotlin("plugin.allopen")
}

description = "RADAR Schemas core specification and validation tools."

dependencies {
    api("org.apache.avro:avro:${Versions.avro}") {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
    api("jakarta.validation:jakarta.validation-api:${Versions.jakartaValidation}")
    api(project(":radar-schemas-commons"))
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    api(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    api("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("io.confluent:kafka-connect-avro-data:${Versions.confluent}") {
        exclude(group = "org.glassfish.jersey.core", module = "jersey-common")
        exclude(group = "jakarta.ws.rs", module = "jakarta.ws.rs-api")
        exclude(group = "io.swagger", module = "swagger-annotations")
        exclude(group = "io.confluent", module = "common-utils")
        exclude(group = "io.confluent", module = "kafka-schema-serializer")
    }

    implementation("org.apache.kafka:connect-api:${Versions.kafka}") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
        exclude(group = "javax.ws.rs", module = "javax.ws.rs-api")
    }

    api("com.squareup.okhttp3:okhttp:${Versions.okHttp}")
    api("org.radarbase:radar-commons-server:${Versions.radarCommons}")
}

allOpen {
    annotation("org.radarbase.config.OpenConfig")
}
