# RADAR Schemas tools

A number of tools are provided with RADAR-Schemas. They are most easily accessed by using the docker
image as described in the main readme. Below is more information about the schema validation that
the tools can perform.

The RADAR Schemas Validator checks if the `Schema Catalog` is in a valid state.

It first checks the folder structure, it has to be compliant with:

- commons
  * active
  * catalogue
  * kafka
  * monitor
  * passive
  * stream
- rest
- specification
  * active
  * monitor
  * passive
  * stream

For each Avro schema under `commons` folder checks if:

- in case of `ENUM`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * there is documentation
  * `symbols` match `UPPER_CASE` format
- in case of `RECORD`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * fields is not empty
  * the `field name` is a lowerCamelCase string and does not contain string such as `value`, `Value`
    .
  * schemas under `active` folder have `time` and `timeCompleted` fields, and do not contain a field
    named `timeReceived`
  * schemas under `monitor` folder have `time` field, and do not contain a field named
    either `timeCompleted` or `timeReceived`
  * schemas under `passive` folder have `time` and `timeReceived` fields, and do not have a field
    named `timeCompleted`
  * the record and any provided fields are documented
  * `ENUM` fields have `UNKNOWN` as `default` value, if any
  * `nullable`/`optional` fields have `null` as default value

Upon rule violation, the end user is notified with a message explaining how to fix it.

## How to use

The validation is implemented as a `JUnit` test. To run the validation, simply type `./gradlew test`

## Suppress checks

Record name and field name validations can be suppressed modifying
the [skip](src/test/resources/schema.yml) configuration file.

`files` lists file paths that can be ignored. It can contain values like

- entire path like `commons/active/questionnaire/questionnaire.avsc`
- folder and subfolder `commons/active/**/*`: all file under `active` and all its subfolder will be
  skipped
- folder and subfolder `commons/active/**/*.avsc`: all file with format `avsc` under `active` and
  all its subfolder will be skipped
- file name `.DS_Store`: all file named `.DS_Store` will be skipped
- file extension `*.md`: all file with extension `*.md` will be skipped

```yaml
files:
  - path/to/avoid/README.md
  - path/to/**/*
  - path/to/**/README.md
  - .DS_Store
  - *.md
``` 

`validation` allows the user to specify checks that should be skipped at schema level:

- a key like `org.radarcns.passive.biovotion.*` set a skip configuration valid for all schemas
  under `org.radarcns.passive.biovotion` package
- a key like `org.radarcns.passive.biovotion.BiovotionVSMSpO2` specify a configuration valid only
  for the given record
- `name_record_check: DISABLE` suppresses the record name check
- `fields` lists field names for which the field name check is suppressed All tests are enable by
  default.

```yaml
validation:
  org.radarcns.passive.biovotion.BiovotionVSMSpO2:
    - fields:
        - fieldnameOne
        - fieldnameTwo
``` 
