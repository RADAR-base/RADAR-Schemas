import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    id("com.github.davidmc24.gradle.plugin.avro-base")
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
    api("org.apache.avro:avro:${Versions.avro}") {
        api("com.fasterxml.jackson.core:jackson-core:${Versions.jackson}")
        api("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}")
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
}

// ---------------------------------------------------------------------------//
// Clean settings                                                            //
// ---------------------------------------------------------------------------//
tasks.clean {
    delete(generateAvro.map { it.outputs })
}
