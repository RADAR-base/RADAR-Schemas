# RADAR-Schemas

[Avro schemas](https://avro.apache.org/docs/1.9.2/spec.html) used in RADAR-base. The schemas are organized as follows:

- The `commons` directory contains all schemas used inside Kafka and data fed into Kafka.
  - In the `active` subdirectory, add schemas for active data collection, like questionnaires or assignments.
  - In the `catalogue` subdirectory, modify schemas for cataloguing data types.
  - In the `kafka` subdirectory, add schemas used throughtout Kafka, like record keys.
  - In the `monitor` subdirectory, add schemas for monitoring applications that gather data.
  - In the `passive` subdirectory, add schemas for passive data collection, like wearables.
  - In the `stream` subdirectory, add schemas used in Kafka Streams.
- The `specifications` directory contains specifications of what data types are collected through which devices.
  - Java SDKs for each of the components are provided in the `java-sdk` folder, see installation instructions there. They are automatically generated from the Avro schemas using the Avro specification (version in [Versions.kt](java-sdk/buildSrc/src/main/kotlin/Versions.kt)).

## Usage

This project can be used in RADAR-base by using the `radarbase/kafka-init` Docker image. The schemas and specifications can be extended by locally creating a directory structure that includes a `commons` and `specifications` directory and mounting it to the image, to the `/schema/conf/commons` and `/schema/conf/specifications` directories, respectively. You can provide a file path in `CONFIG_YAML` that points to a `config.yaml` file that is mounted in the docker container. The config file has the following format:

```yaml
# Specify any Kafka properties needed to connect to the Kafka cluster
kafka:
  security.protocol: PLAINTEXT

# Configure additional topics, or specify the properties of a topic listed elsewhere.
topics:
  my_custom_topic:
    # Enable configuration of this topic
    enabled: true
    # Number of partitions to use, if newly created
    partitions: 3
    # Replication factor, if newly created
    replicationFactor: 2
    # Topic properties in Kafka. See
    # <https://docs.confluent.io/platform/current/installation/configuration/topic-configs.html#ak-topic-configurations-for-cp>
    properties:
      cleanup.policy: compact
    # Key schema for the topic
    keySchema: my.key.Schema
    # Value schema for the topic
    valueSchema: my.value.Schema
    # Whether to register the schemas to the Schema Registry
    registerSchema: false

# Schema configuration. This refers to the files in the commons directory.
schemas:
  # Only include given schema directory files. You can use File glob syntax as described in <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String->
  # If include is specified, exclude will be ignored. The glob pattern should start from the commons directory.
  include: []
  # Exclude all given schema directory files. You can use File glob syntax as described in <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String->
  # If include is specified, exclude will be ignored. The glob pattern should start from the commons directory.
  exclude:
    - active/**
  # You can specify additional schemas, using the format for each respective specification directory.
  monitor:
    # The object name is the path it would have, the value is a plain string containing a JSON object
    application/application_uptime2.avsc: >
      {
        "namespace": "org.radarcns.monitor.application",
        "type": "record",
        "name": "ApplicationUptime2",
        "doc": "Length of application uptime.",
        "fields": [
          { "name": "time", "type": "double", "doc": "Device timestamp in UTC (s)." },
          { "name": "uptime", "type": "double", "doc": "Time since last app start (s)." }
        ]
      }

# Source configuration. This refers to the files in the specifications directory.
sources:
  # Only include given specification directory files. You can use File glob syntax as described in <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String->
  # If include is specified, exclude will be ignored. The glob pattern should start from the specifications directory.
  include:
    - passive/*
  # Exclude all given specification directory files. You can use File glob syntax as described in <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String->
  # If include is specified, exclude will be ignored. The glob pattern should start from the specifications directory.
  exclude: []
  # You can specify additional sources, using the format for each respective specification directory.
  monitor:
    - vender: test
      model: test
      version: 1.0.0
      data:
        type: UPTIME
        topic: application_uptime2
        value_schema: .monitor.application.ApplicationUptime2
```

Please see the inline comments for more information on their values.

## Contributing

The Avro schemas should follow the [Google JSON style guide](https://google.github.io/styleguide/jsoncstyleguide.xml).

In addition, schemas in the `commons` directory should follow the following guidelines:

- Try to avoid abbreviations in the field names and write out the field name instead.
- There should be no need to add `value` at the end of a field name.
- Enumerator items should be written in uppercase characters separated by underscores.
- Add documentation (the `doc` property) to each schema, each field, and each enum. The documentation should show in text what is being measured, how, and what units or ranges are applicable. Abbreviations and acronyms in the documentation should be written out. Each doc property should start with a capital and end with a period.
- Prefer a categorical specification (an Avro enum) over a free string if that categorization is expected to remain very stable. This disambiguates the possible values for analysis. If a field is expected to be extended outside this project or very often within this project, use a free string instead.
- Prefer a flat record over a hierarchical record. This simplifies the organization of the data downstream, for example, when mapping to CSV.
- Prefer written out fields to arrays. This simplifies the organization of the data downstream, for example, when mapping to CSV.
- Give each schema a proper namespace, preferably `org.radarcns.passive.<vendor>` fully in lowercase, without any numbers, uppercase letters or symbols (except `.`). For the Empatica E4, the vendor is Empatica, so the namespace is `org.radarcns.passive.empatica`. For generic types, like a phone, Android Wear device or Android application, the namespace could just be `org.radarcns.passive.phone`, `org.radarcns.passive.wear`, or `org.radarcns.monitor.application`.
- In the schema name, use upper camel case and name the device explicitly (for example, `EmpaticaE4Temperature`).
- For fields that are inherent to a record, and will never be removed or renamed, no default value is needed. For all other fields:
  - if the type is an enum, use an `UNKNOWN` symbol as default value
  - otherwise, set the type to a union of `["null", <intended type>]` and set the default value to `null`.

### Validation phase

Avro schemas are automatically validated against RADAR-base guide lines while building. For more details, check [catalog validator](java-sdk/radar-schemas-tools).

### Test setup

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
docker-compose run --rm tools radar-schemas-tools validate /schema/merged
# list topic information
docker-compose run --rm tools radar-schemas-tools list /schema/merged
# register schemas with the schema registry
docker-compose run --rm tools radar-schemas-tools register http://schema-registry-1:8081 /schema/merged
# create topics with zookeeper
docker-compose run --rm tools radar-schemas-tools create -s kafka-1:9092 -b 1 -r 1 -p 1 /schema/merged
# run source-catalogue webservice
docker-compose run -p 8080:8080 --rm tools radar-catalog-server -p 8080 /schema/merged
# and in a separate console, run
curl localhost:8080/source-types
# back up the _schemas topic
docker-compose run --rm tools radar-schemas-tools schema-topic --backup -f schema.json -b 1 -s kafka-1:9092 -f /schema/conf/backup.json /schema/merged
# ensure the validity of the _schemas topic
docker-compose run --rm tools radar-schemas-tools schema-topic --ensure -f schema.json -b 1 -s kafka-1:9092 -f /schema/conf/backup.json -r 1 /schema/merged
```

### Using radar-schema-tools with Confluent Cloud

1. Create topics on Confluent Cloud 

    1.1. Create a `config.yaml` file. A Confluent Cloud config for Java application based on this [template](https://github.com/confluentinc/configuration-templates/blob/master/clients/cloud/java-sr.config).

    ```yaml
    
    kafka:
      bootstrap.servers: {{ BROKER_ENDPOINT }}
      security.protocol: SASL_SSL
      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="{{ CLUSTER_API_KEY }}" password="{{ CLUSTER_API_SECRET }}";
      ssl.endpoint.identification.algorithm: https
      sasl.mechanism: PLAIN
    ```

    1.2. Run `create` command

    ```
    docker run --rm -v "$PWD/config.yaml:/etc/radar-schemas-tools/config.yaml" radarbase/radar-schemas-tools radar-schemas-tools create -c /etc/radar-schemas-tools/config.yaml /schema/merged
    ```

2. Register schemas on Confluent Cloud schema registry

    ```
    docker run --rm -v "$PWD/config.yaml:/etc/radar-schemas-tools/config.yaml" radarbase/radar-schemas-tools radar-schemas-tools register -c /etc/radar-schemas-tools/config.yaml -u SR_API_KEY -p SR_API_SECRET SR_ENDPOINT /schema/merged
    ```
   
   Note that the `SR_ENDPOINT` and `/schema/merged` are positional arguments and should be placed at the end of the command.
