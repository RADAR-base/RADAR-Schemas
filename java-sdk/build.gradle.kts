import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    id("org.radarbase.radar-root-project") version Versions.radarCommons
    id("org.radarbase.radar-dependency-management") version Versions.radarCommons
    id("org.radarbase.radar-kotlin") version Versions.radarCommons apply false
    id("org.radarbase.radar-publishing") version Versions.radarCommons apply false
    id("com.github.davidmc24.gradle.plugin.avro-base") version Versions.avroGenerator apply false
    kotlin("plugin.allopen") version Versions.kotlin apply false
}

radarRootProject {
    projectVersion.set(Versions.project)
    gradleVersion.set(Versions.gradle)
}

// Configuration
val githubRepoName = "RADAR-base/RADAR-Schemas"
val githubUrl = "https://github.com/${githubRepoName}.git"
val githubIssueUrl = "https://github.com/$githubRepoName/issues"

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")

    repositories{
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }

    radarKotlin {
        javaVersion.set(Versions.java)
        kotlinVersion.set(Versions.kotlin)
        slf4jVersion.set(Versions.slf4j)
        log4j2Version.set(Versions.log4j2)
        junitVersion.set(Versions.junit)
    }

    afterEvaluate {
        configurations.all {
            exclude(group = "org.slf4j", module = "slf4j-log4j12")
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

// Configure libraries
configure(listOf(
    project(":radar-schemas-commons"),
    project(":radar-schemas-core"),
    project(":radar-schemas-registration")
)) {
    apply(plugin = "java-library")
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        javaVersion.set(Versions.java)
    }

    radarPublishing {
        githubUrl.set("https://github.com/$githubRepoName")
        developers {
            developer {
                id.set("bdegraaf1234")
                name.set("Bastiaan de Graaf")
                email.set("bastiaan@thehyve.nl")
                organization.set("The Hyve")
            }
            developer {
                id.set("nivemaham")
                name.set("Nivethika Mahasivam")
                email.set("nivethika@thehyve.nl")
                organization.set("The Hyve")
            }
        }
    }
}
