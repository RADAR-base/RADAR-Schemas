rootProject.name = "radar-schemas"

include(":radar-schemas-registration")
include(":radar-schemas-tools")
include(":radar-schemas-commons")
include(":radar-catalog-server")
include(":radar-schemas-core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}
