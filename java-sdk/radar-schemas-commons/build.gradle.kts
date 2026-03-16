import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    alias(libs.plugins.avro.base)
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
    setOutputDir(layout.buildDirectory.dir("generated/java").get().asFile)
}

sourceSets {
    main {
        java.srcDir(generateAvro)
    }
}

dependencies {
    api(libs.apache.avro) {
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
}

// ---------------------------------------------------------------------------//
// Clean settings                                                            //
// ---------------------------------------------------------------------------//
tasks.clean {
    delete(generateAvro)
}
