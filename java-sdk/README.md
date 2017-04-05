# RADAR Schemas Java SDK

The Java SDKs are published as JARs on bintray. To use them in Gradle, add the following code to your `build.gradle`:

```gradle
repositories {
    maven { url  'http://dl.bintray.com/radar-cns/org.radarcns' }
}

dependencies {
    // Commons schemas (backend, passive remote monitoring app)
    compile 'org.radarcns:radar-schemas-commons:0.1'

    // REST API schemas (REST API, testing)
    compile 'org.radarcns:radar-schemas-restapi:0.1'

    // Questionnaire schemas (active remote monitoring app)
    compile 'org.radarcns:radar-schemas-questionnaire:0.1'
}
```
Usually, you only need to include the schemas you actually need in your dependencies.
