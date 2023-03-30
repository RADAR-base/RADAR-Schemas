import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    id("org.radarbase.radar-root-project") version Versions.radarCommons
    id("org.radarbase.radar-dependency-management") version Versions.radarCommons
    id("org.radarbase.radar-kotlin") version Versions.radarCommons apply false
    id("org.radarbase.radar-publishing") version Versions.radarCommons apply false
}

radarRootProject {
    projectVersion.set(Versions.project)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")

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

    tasks.withType<Test> {
        inputs.dir("${project.rootDir}/../commons")
        inputs.dir("${project.rootDir}/../specifications")
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
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        javaVersion.set(11)
    }

    radarPublishing {
        val githubRepoName = "RADAR-base/radar-schemas"
        githubUrl.set("https://github.com/$githubRepoName.git")

        developers {
            developer {
                id.set("blootsvoets")
                name.set("Joris Borgdorff")
                email.set("joris@thehyve.nl")
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
