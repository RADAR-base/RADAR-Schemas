# RADAR-Schemas

[![Build Status](https://travis-ci.org/RADAR-CNS/RADAR-Schemas.svg?branch=master)](https://travis-ci.org/RADAR-CNS/RADAR-Schemas)

[Avro schemas](https://avro.apache.org/docs/1.8.2/spec.html) used in RADAR-CNS. The schemas are organized as follows:

- The `commons` directory contains all schemas used inside Kafka and data fed into Kafka.
  - In the `active` subdirectory, add schemas for active data collection, like questionnaires or assignments.
  - In the `catalogue` subdirectory, modify schemas for cataloguing data types.
  - In the `kafka` subdirectory, add schemas used throughtout Kafka, like record keys.
  - In the `monitor` subdirectory, add schemas for monitoring applications that gather data.
  - In the `passive` subdirectory, add schemas for passive data collection, like wearables.
  - In the `stream` subdirectory, add schemas used in Kafka Streams.
- The `restapi` directory contains schemas used to get data from the RADAR-CNS REST API.
- The `specifications` directory contains specifications of what data types are collected through which devices.
- Java SDKs for each of the components are provided in the `java-sdk` folder, see installation instructions there. They are automatically generated from the Avro schemas using the Avro 1.8.2 specification.

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

Avro schemas are automatically validated against RADAR-CNS guide lines while building. For more details, check [catalog validator](java-sdk/radar-schemas-tools).

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
```
