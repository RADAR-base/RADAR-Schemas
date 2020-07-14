# RADAR-Schemas

[![Build Status](https://travis-ci.org/RADAR-base/RADAR-Schemas.svg?branch=master)](https://travis-ci.org/RADAR-base/RADAR-Schemas)

[Avro schemas](https://avro.apache.org/docs/1.8.2/spec.html) used in RADAR-base. The schemas are organized as follows:

- The `commons` directory contains all schemas used inside Kafka and data fed into Kafka.
  - In the `active` subdirectory, add schemas for active data collection, like questionnaires or assignments.
  - In the `catalogue` subdirectory, modify schemas for cataloguing data types.
  - In the `kafka` subdirectory, add schemas used throughtout Kafka, like record keys.
  - In the `monitor` subdirectory, add schemas for monitoring applications that gather data.
  - In the `passive` subdirectory, add schemas for passive data collection, like wearables.
  - In the `stream` subdirectory, add schemas used in Kafka Streams.
- The `specifications` directory contains specifications of what data types are collected through which devices.
- Java SDKs for each of the components are provided in the `java-sdk` folder, see installation instructions there. They are automatically generated from the Avro schemas using the Avro 1.8.2 specification.

## Usage

This project can be used in RADAR-base by using the `radarbase/kafka-init` Docker image. The schemas and specifications can be extended by locally creating a directory structure that includes a `commons` and `specifications` directory and mounting it to the image, to the `/schema/conf/commons` and `/schema/conf/specifications` directories, respectively. Existing specifications can be excluded from your deployment by mounting a file at `/etc/radar-schemas/specifications.exclude`, with on each line a file pattern that can be excluded. The pattern should start from the `specifications` directory as parent directory. Example file contents:
```
active/*
passive/biovotion*
```

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
docker-compose run --rm tools radar-schemas-tools validate
# list topic information
docker-compose run --rm tools radar-schemas-tools list
# register schemas with the schema registry
docker-compose run --rm tools radar-schemas-tools register http://schema-registry:8081
# create topics with zookeeper
docker-compose run --rm tools radar-schemas-tools create zookeeper-1:2181
# run source-catalogue webservice
docker-compose run --rm tools radar-schemas-tools serve -p <portnumber>
# back up the _schemas topic
docker-compose run --rm tools radar-schemas-tools schema-topic --backup -f schema.json -b 1 zookeeper-1:2181
# ensure the validity of the _schemas topic
docker-compose run --rm tools radar-schemas-tools schema-topic --ensure -f schema.json -b 1 -r 1 zookeeper-1:2181
```

### Using radar-schema-tools with Confluent Cloud

1. Create topics on Confluent Cloud 

    1.1. Create a `java-config.properties` file. A Confluent Cloud config for Java application based on this [template](https://github.com/confluentinc/configuration-templates/blob/master/clients/cloud/java-sr.config).

    ```properties
    # Kafka
    bootstrap.servers={{ BROKER_ENDPOINT }}
    security.protocol=SASL_SSL
    sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="{{ CLUSTER_API_KEY }}" password="{{ CLUSTER_API_SECRET }}";
    ssl.endpoint.identification.algorithm=https
    sasl.mechanism=PLAIN
    ```
    1.2. Run `cc-topic-create` command

    ```
    docker run --rm -v "$PWD/java-config.properties:/schema/conf/java.properties" radarbase/kafka-init radar-schemas-tools cc-topic-create -c java-config.properties
    ```
        
2. Register schemas on Confluent Cloud schema registry

    ```
    docker run --rm radarbase/kafka-init radar-schemas-tools register SR_ENDPOINT -u SR_API_KEY -p SR_API_SECRET
    ```
