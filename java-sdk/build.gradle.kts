plugins {
    id("org.radarbase.radar-root-project")
    id("org.radarbase.radar-dependency-management")
    id("org.radarbase.radar-kotlin")
}

radarRootProject {
    projectVersion.set(properties["projectVersion"] as String)
    gradleVersion.set(libs.versions.gradle)
}
