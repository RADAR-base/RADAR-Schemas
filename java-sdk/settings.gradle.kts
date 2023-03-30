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
        maven(url = "https://maven.pkg.github.com/radar-base/radar-commons") {
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: extra.properties["gpr.user"] as? String
                    ?: extra.properties["public.gpr.user"] as? String
                password = System.getenv("GITHUB_TOKEN")
                    ?: extra.properties["gpr.token"] as? String
                    ?: (extra.properties["public.gpr.token"] as? String)?.let {
                        java.util.Base64.getDecoder().decode(it).decodeToString()
                    }
            }
        }
    }
}
