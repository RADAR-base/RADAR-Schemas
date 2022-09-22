description = "RADAR Schemas core specification and validation tools."

dependencies {
    val avroVersion: String by project
    api("org.apache.avro:avro:$avroVersion") {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
    val javaxValidationVersion: String by project
    api("javax.validation:validation-api:$javaxValidationVersion")
    api(project(":radar-schemas-commons"))

    val jacksonVersion: String by project
    api(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    api("com.fasterxml.jackson.core:jackson-databind")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    val confluentVersion: String by project
    implementation("io.confluent:kafka-connect-avro-data:$confluentVersion") {
        exclude(group = "org.glassfish.jersey.core", module = "jersey-common")
        exclude(group = "jakarta.ws.rs", module = "jakarta.ws.rs-api")
        exclude(group = "io.swagger", module = "swagger-annotations")
        exclude(group = "io.confluent", module = "common-utils")
        exclude(group = "io.confluent", module = "kafka-schema-serializer")
    }

    val kafkaVersion: String by project
    implementation("org.apache.kafka:connect-api:$kafkaVersion") {
        exclude(group = "org.apache.kafka", module = "kafka-clients")
        exclude(group = "javax.ws.rs", module = "javax.ws.rs-api")
    }

    val okHttpVersion: String by project
    val radarCommonsVersion: String by project
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")
    api("org.radarbase:radar-commons-server:$radarCommonsVersion")
}
