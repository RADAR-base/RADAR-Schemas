description = "RADAR Schemas specification and validation tools"

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    api(project(":radar-schemas-commons"))
    api(project(":radar-schemas-core"))
    val okHttpVersion: String by project
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")
    val radarCommonsVersion: String by project
    api("org.radarbase:radar-commons-server:$radarCommonsVersion")

    val jacksonVersion: String by project
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    val nettyVersion: String by project
    implementation(platform("io.netty:netty-bom:$nettyVersion"))
    val jettyVersion: String by project
    implementation(platform("org.eclipse.jetty:jetty-bom:$jettyVersion"))

    val confluentVersion: String by project
    implementation("io.confluent:kafka-connect-avro-converter:$confluentVersion")
    implementation("io.confluent:kafka-schema-registry:$confluentVersion") {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }
    val kafkaVersion: String by project
    implementation("org.apache.kafka:connect-json:$kafkaVersion")

    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
}
