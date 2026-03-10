import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    alias(libs.plugins.radar.root.project)
    alias(libs.plugins.radar.dependency.management)
    alias(libs.plugins.radar.kotlin) apply false
    alias(libs.plugins.radar.publishing) apply false
    alias(libs.plugins.avro.base) apply false
    alias(libs.plugins.kotlin.allopen) apply false
}

radarRootProject {
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

val githubRepoName = "RADAR-base/RADAR-Schemas"
val githubUrl = "https://github.com/${githubRepoName}.git"
val githubIssueUrl = "https://github.com/$githubRepoName/issues"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.radarbase.radar-kotlin")

    repositories{
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }

    radarKotlin {
        javaVersion.set(rootProject.libs.versions.java.get().toInt())
        kotlinVersion.set(rootProject.libs.versions.kotlin)
        slf4jVersion.set(rootProject.libs.versions.slf4j)
        log4j2Version.set(rootProject.libs.versions.log4j2)
        junitVersion.set(rootProject.libs.versions.junit)
    }

    configurations.all {
        resolutionStrategy {
            /* The entries in the block below are added here to force the version of
            *  transitive dependencies and mitigate reported vulnerabilities */
            force(
                "com.fasterxml.jackson.core:jackson-databind:2.17.2",
                "org.apache.commons:commons-lang3:3.18.0"
            )
        }
    }
}

// Configure applications
configure(listOf(
    project(":radar-schemas-tools"),
    project(":radar-catalog-server"),
)) {
    apply(plugin = "application")
}

configure(listOf(
    project(":radar-schemas-commons"),
    project(":radar-schemas-core"),
    project(":radar-schemas-registration")
)) {
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        javaVersion.set(rootProject.libs.versions.java.get().toInt())
        kotlinVersion.set(rootProject.libs.versions.kotlin)
        slf4jVersion.set(rootProject.libs.versions.slf4j)
        log4j2Version.set(rootProject.libs.versions.log4j2)
    }

    radarPublishing {
        githubUrl.set("https://github.com/$githubRepoName")
        developers {
            developer {
                id.set("pvannierop")
                name.set("Pim van Nierop")
                email.set("pim@thehyve.nl")
                organization.set("The Hyve")
            }
        }
    }
}
