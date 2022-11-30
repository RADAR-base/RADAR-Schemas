import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    id("com.github.davidmc24.gradle.plugin.avro-base")
}

// Generated avro files
val avroOutputDir = file("$projectDir/src/generated/java")

description = "RADAR Schemas Commons SDK"

sourceSets {
    main {
        java.srcDir(avroOutputDir)
    }
}

dependencies {
    val avroVersion: String by project
    val jacksonVersion: String by project
    api("org.apache.avro:avro:$avroVersion") {
        api("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
        api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
        exclude(group = "org.xerial.snappy", module = "snappy-java")
        exclude(group = "com.thoughtworks.paranamer", module = "paranamer")
        exclude(group = "org.apache.commons", module = "commons-compress")
        exclude(group = "org.tukaani", module = "xz")
    }
}

//---------------------------------------------------------------------------//
// Clean settings                                                            //
//---------------------------------------------------------------------------//
tasks.clean {
    delete(avroOutputDir)
}

//---------------------------------------------------------------------------//
// AVRO file manipulation                                                    //
//---------------------------------------------------------------------------//
val generateAvro by tasks.registering(GenerateAvroJavaTask::class) {
    source(rootProject.fileTree("../commons") {
        include("**/*.avsc")
    })
    setOutputDir(avroOutputDir)
}

tasks["compileJava"].dependsOn(generateAvro)
tasks["compileKotlin"].dependsOn(generateAvro)
tasks["dokkaJavadoc"].dependsOn(generateAvro)
