import org.radarbase.gradle.plugin.radarKotlin

plugins {
    id("org.radarbase.radar-kotlin")
}

radarKotlin {
    // TODO remove after using new release of radar-kotlin plugin
    javaVersion.set(17)
}
