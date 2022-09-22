rootProject.name = "radar-schemas"

include(":radar-schemas-registration")
include(":radar-schemas-tools")
include(":radar-schemas-commons")
include(":radar-catalog-server")
include(":radar-schemas-core")

pluginManagement {
    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    val nexusPluginVersion: String by settings
    val dependencyUpdateVersion: String by settings
    val avroGeneratorVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPluginVersion
        id("com.github.ben-manes.versions") version dependencyUpdateVersion
        id("com.github.davidmc24.gradle.plugin.avro-base") version avroGeneratorVersion
    }
}
