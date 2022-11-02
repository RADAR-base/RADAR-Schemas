import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.github.gradle-nexus.publish-plugin")
    id("com.github.ben-manes.versions")
    kotlin("jvm") apply false
    id("org.jetbrains.dokka") apply false
}

allprojects {
    version = "0.8.1"
    group = "org.radarbase"
}

// Configuration
val githubRepoName = "RADAR-base/RADAR-Schemas"
val githubUrl = "https://github.com/${githubRepoName}.git"
val githubIssueUrl = "https://github.com/$githubRepoName/issues"

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven(url = "https://packages.confluent.io/maven/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }

    afterEvaluate {
        configurations.all {
            resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
            resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
            exclude(group = "org.slf4j", module = "slf4j-log4j12")
        }
    }

    enableTesting()

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }
}

// Configure applications
configure(listOf(
    project(":radar-schemas-tools"),
    project(":radar-catalog-server"),
)) {
    apply(plugin = "application")

    extensions.configure(JavaApplication::class) {
        applicationDefaultJvmArgs = listOf(
            "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager",
        )
    }

    setJavaVersion(17)

    tasks.withType<Tar> {
        compression = Compression.GZIP
        archiveExtension.set("tar.gz")
    }

    tasks.register("downloadDependencies") {
        configurations.named("compileClasspath").map { it.files }
        configurations.named("runtimeClasspath").map { it.files }
        doLast {
            println("Downloaded compile-time dependencies")
        }
    }

    tasks.register<Copy>("copyDependencies") {
        from(configurations.named("runtimeClasspath").map { it.files })
        into("$buildDir/third-party/")
        doLast {
            println("Copied third-party runtime dependencies")
        }
    }
}

// Configure libraries
configure(listOf(
    project(":radar-schemas-commons"),
    project(":radar-schemas-core"),
    project(":radar-schemas-registration")
)) {
    apply(plugin = "java-library")

    setJavaVersion(11)

    enableDokka()

    enablePublishing()
}

tasks.withType<DependencyUpdatesTask> {
    val stableVersionPattern = "(RELEASE|FINAL|GA|-ce|^[0-9,.v-]+)$".toRegex(RegexOption.IGNORE_CASE)

    rejectVersionIf {
        !stableVersionPattern.containsMatchIn(candidate.version)
    }
}

nexusPublishing {
    fun Project.propertyOrEnv(propertyName: String, envName: String): String? {
        return if (hasProperty(propertyName)) {
            property(propertyName)?.toString()
        } else {
            System.getenv(envName)
        }
    }

    repositories {
        sonatype {
            username.set(propertyOrEnv("ossrh.user", "OSSRH_USER"))
            password.set(propertyOrEnv("ossrh.password", "OSSRH_PASSWORD"))
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.5.1"
}

/** Set the given Java [version] for compiled Java and Kotlin code. */
fun Project.setJavaVersion(version: Int) {
    tasks.withType<JavaCompile> {
        options.release.set(version)
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = version.toString()
            languageVersion = "1.7"
            apiVersion = "1.7"
        }
    }
}

/** Add JUnit testing and logging, PMD, and Checkstyle to a project. */
fun Project.enableTesting() {
    dependencies {
        val log4j2Version: String by project
        val testRuntimeOnly by configurations
        testRuntimeOnly("org.apache.logging.log4j:log4j-core:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")

        val junitVersion: String by project
        val testImplementation by configurations
        testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    }

    tasks.withType<Test> {
        systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
        useJUnitPlatform()
        inputs.dir("${project.rootDir}/../commons")
        inputs.dir("${project.rootDir}/../specifications")
        testLogging {
            events("skipped", "failed")
            setExceptionFormat("full")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }
    }

    apply(plugin = "checkstyle")

    tasks.withType<Checkstyle> {
        ignoreFailures = false

        configFile = file("$rootDir/config/checkstyle/checkstyle.xml")

        source = fileTree("$projectDir/src/main/java") {
            include("**/*.java")
        }
    }

    apply(plugin = "pmd")

    tasks.withType<Pmd> {
        ignoreFailures = false

        source = fileTree("$projectDir/src/main/java") {
            include("**/*.java")
        }

        isConsoleOutput = true

        ruleSets = listOf()

        ruleSetFiles = files("$rootDir/config/pmd/ruleset.xml")
    }
}

/** Enable Dokka documentation generation for a project. */
fun Project.enableDokka() {
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        val dokkaVersion: String by project
        val dokkaHtmlPlugin by configurations
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")

        val jacksonVersion: String by project
        val dokkaPlugin by configurations
        dokkaPlugin(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
        val dokkaRuntime by configurations
        dokkaRuntime(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))

        val jsoupVersion: String by project
        dokkaPlugin("org.jsoup:jsoup:$jsoupVersion")
        dokkaRuntime("org.jsoup:jsoup:$jsoupVersion")
    }
}

/** Enable publishing a project to a Maven repository. */
fun Project.enablePublishing() {
    val myProject = this

    val sourcesJar by tasks.registering(Jar::class) {
        from(myProject.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val classes by tasks
        dependsOn(classes)
    }

    val dokkaJar by tasks.registering(Jar::class) {
        from("$buildDir/dokka/javadoc")
        archiveClassifier.set("javadoc")
        val dokkaJavadoc by tasks
        dependsOn(dokkaJavadoc)
    }

    val assemble by tasks
    assemble.dependsOn(sourcesJar)
    assemble.dependsOn(dokkaJar)

    apply(plugin = "maven-publish")

    val mavenJar by extensions.getByType<PublishingExtension>().publications.creating(MavenPublication::class) {
        from(components["java"])

        artifact(sourcesJar)
        artifact(dokkaJar)

        afterEvaluate {
            pom {
                name.set(myProject.name)
                description.set(myProject.description)
                url.set(githubUrl)
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
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
                issueManagement {
                    system.set("GitHub")
                    url.set(githubIssueUrl)
                }
                organization {
                    name.set("RADAR-base")
                    url.set("https://radar-base.org")
                }
                scm {
                    connection.set("scm:git:$githubUrl")
                    url.set(githubUrl)
                }
            }
        }
    }

    apply(plugin = "signing")

    extensions.configure(SigningExtension::class) {
        useGpgCmd()
        isRequired = true
        sign(tasks["sourcesJar"], tasks["dokkaJar"])
        sign(mavenJar)
    }

    tasks.withType<Sign> {
        onlyIf { gradle.taskGraph.hasTask(myProject.tasks["publish"]) }
    }
}
