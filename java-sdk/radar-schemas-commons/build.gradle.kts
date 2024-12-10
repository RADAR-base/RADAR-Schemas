import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    id("java-library")
    id("publishing-convention")
    alias(libs.plugins.davidmc24.avro.base)
}

description = "RADAR Schemas Commons SDK"

// ---------------------------------------------------------------------------//
// AVRO file manipulation                                                    //
// ---------------------------------------------------------------------------//
val generateAvro by tasks.registering(GenerateAvroJavaTask::class) {
    source(
        rootProject.fileTree("../commons") {
            include("**/*.avsc")
        },
    )
    setOutputDir(layout.projectDirectory.dir("src/generated/java").asFile)
}

sourceSets {
    main {
        java.srcDir(generateAvro.map { it.outputs })
    }
}

dependencies {
    api(libs.avro) {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
    api(libs.jackson.core)
    api(libs.jackson.databind)
}

// ---------------------------------------------------------------------------//
// Clean settings                                                            //
// ---------------------------------------------------------------------------//
tasks.clean {
    delete(generateAvro.map { it.outputs })
}
