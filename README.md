# RADAR-Schemas

[![Build Status](https://travis-ci.org/RADAR-CNS/RADAR-Schemas.svg?branch=master)](https://travis-ci.org/RADAR-CNS/RADAR-Schemas)

[Avro schemas](https://avro.apache.org/docs/1.8.1/spec.html) used in RADAR-CNS. The schemas are divided into three parts: `commons` for the passive remote monitoring application and the backend, `restapi` for the REST API, and `questionnaire` for the active remote monitoring application.

Java SDKs for each of the components are provided in the `java-sdk` folder, see installation instructions there. They are automatically generated from the Avro schemas using the Avro 1.8.1 specification.

## Contributing

The Avro schemas should follow the [Google JSON style guide](https://google.github.io/styleguide/jsoncstyleguide.xml).

In addition, schemas in the `commons` directory should follow the following guidelines:

- Try to avoid abbreviations in the field names and write out the field name instead.
- There should be no need to add `value` at the end of a field name.
- Enumerator items should be written in uppercase characters separated by underscores.
- Add documentation (the `doc` property) to each schema and each field. The documentation should show in text what is being measured, how, and what units or ranges are applicable. Abbreviations and acronyms in the documentation should be written out.
- Prefer a categorical specification (an Avro enum) over a free string. This disambiguates the possible values for analysis.
- Prefer a flat record over a hierarchical record. This simplifies the organization of the data downstream, for example, when mapping to CSV.
- Prefer written out fields to arrays. This simplifies the organization of the data downstream, for example, when mapping to CSV.
- Give each schema a proper namespace, preferably `org.radarcns.deviceproducer` fully in lowercase, without any numbers, uppercase letters or symbols (except `.`). For the Empatica E4, the producer is Empatica, so the namespace is `org.radarcns.empatica`. For generic types, like a phone, Android Wear device or Android application, the namespace could just be `org.radarcns.phone`, `org.radarcns.wear`, or `org.radarcns.application`.
- In the schema name, use upper camel case and name the device explicitly (for example, `EmpaticaE4Temperature`).
