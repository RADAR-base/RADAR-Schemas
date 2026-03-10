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

    // --- Vulnerability fixes start ---
    dependencies {
        constraints {
            add("implementation", rootProject.libs.jackson.bom) {
                because("Force safe version of Jackson across all modules")
            }
            add("implementation", rootProject.libs.apache.commons.lang) {
                because("Force safe version of commons-lang across all modules")
            }
        }
    }

    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // Substitute the old group/module with the new one
            substitute(module("org.lz4:lz4-java"))
                .using(module(rootProject.libs.lz4.get().toString()))
                .because("Force safe version of LZ4 across all modules")
        }
    }
}
// --- Vulnerability fixes end ---

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
