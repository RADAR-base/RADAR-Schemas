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

The generated code each refers to a single schema. The classes of Avro records will extend `org.apache.avro.specific.SpecificRecord`. They each have a static `getClassSchema()` function that returns the `Schema` that it was generated from. To read JSON serialized data for example, use the following code:

```java
public class Deserialize {
   public static void main(String args[]) throws Exception {
      //Instantiating the Schema.Parser class.
      DatumReader<PhoneBatteryLevel> datumReader = new SpecificDatumReader<>(PhoneBatteryLevel.class);
      DataFileReader<PhoneBatteryLevel> dataFileReader = new DataFileReader<>(new File("/path/to/mydata.avro"), datumReader);

      System.out.println("Reading phone battery levels");
      PhoneBatteryLevel batteryLevel = null;
      while (dataFileReader.hasNext()) {
         batteryLevel = dataFileReader.next(batteryLevel);
         System.out.println("Phone battery level: " + batteryLevel);
      }
      System.out.println("Done");
   }
}
```

Alternatively, use `org.radarcns.data.SpecificRecordEncoder` and `org.radarcns.data.SpecificRecordDecoder` from the `radar-commons` package.

## Test setup

The RADAR schema tools can be tested locally using Docker. To run the tools, first install Docker. Then run

```shell
docker-compose build
docker-compose up -d zookeeper-1 kafka-1 schema-registry-1
```
Now you can run tools commands with
```shell
# usage
docker-compose run --rm tools
# validation
docker-compose run --rm tools radar-schemas-tools validate
# list topic information
docker-compose run --rm tools radar-schemas-tools list
# register schemas with the schema registry
docker-compose run --rm tools radar-schemas-tools register http://schema-registry:8081
# create topics with zookeeper
docker-compose run --rm tools radar-schemas-tools create zookeeper-1:2181
```
