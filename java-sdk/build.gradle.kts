plugins {
    id("org.radarbase.radar-root-project")
    id("org.radarbase.radar-dependency-management")
    id("org.radarbase.radar-kotlin")
}

radarRootProject {
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

subprojects {
    apply(plugin = "java-library")

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
    // --- Vulnerability fixes end ---
}
