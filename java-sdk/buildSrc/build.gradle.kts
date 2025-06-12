import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl` // Need for 'convention plugins'
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Gradle plugins used in the convention plugin, are declared as dependencies here.
    implementation(libs.gradle.radar.kotlin)
    implementation(libs.gradle.radar.publishing)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
