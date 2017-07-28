# RADAR Schemas Validator

The RADAR Schemas Validator checks if the `Schema Catalog` is in a valid state.

It first checks the folder structure, it has to be compliant with:
- commons
  * active
  * kafka
  * monitor
  * passive
- rest
- specification
  * active
  * monitor
  * passive
  
For each Avro schema under `commons` folder checks if:
- in case of `ENUM`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * there is documentation
  * `symbols` match UPPER_CASE format
  * the `UNKNOWN` symbol is present
- in case of `RECORD`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * fields is not empty
  * the `field name` is a lowerCamelCase string and does not contain string such as `value`, `Value`, `val` and `Val`.
  * schemas under `active` folder have `time` and `timeCompleted` fields, and do not contain a field named `timeReceived`
  * schemas under `monitor` folder have `time` field, and do not contain a field named either `timeCompleted` or `timeReceived`
  * schemas under `passive` folder have `time` and `timeReceived` fields, and do not have a field named `timeCompleted`
  * the record and any provided fields are documented
  * `ENUM` fields have `UNKNOWN` as `default` value
  * `nullable`/`optional` fields have `null` as default value 
  
The validation process generates a field name collision summary. It is shown to the end user only in presence of collisions.

Upon rule violation, the end user is notified with a message explaining how to fix it.

## How to use

The validation is implemented as a `JUnit` test. To run the validation, simply type `./gradlew test`

## Suppress checks

Record name, field name validations, and field name collision check can be suppressed modifying the [skip](src/test/resources/skip.yml) configuration file.

`files` lists file paths that can be ignored. It can contain values like
- entire path like `commons/active/questionnaire/questionnaire.avsc`
- folder and subfolder `commons/active/**`: all file under `active` and all its subfolder will be skipped
- folder and subfolder `commons/active/**/*.avsc`: all file with format `avsc` under `active` and all its subfolder will be skipped

```yaml
files:
  - path/to/avoid/README.md
  - path/to/**
  - path/to/**/README.md
``` 

`validation` allows the user to specify checks that should be skipped at schema level:
- a key like `org.radarcns.passive.biovotion.*` set a skip configuration valid for all schemas under `org.radarcns.passive.biovotion` package
- a key like `org.radarcns.passive.biovotion.BiovotionVSMSpO2` specify a configuration valid only for the given record
- `name_record_check: DISABLE` suppresses the record name check
- `fields` lists field names for which the field name check is suppressed
All tests are enable by default.

```yaml
validation:
  schema_to_skip:
    - record_name_check: [ ENABLE | DISABLE ]
      fields:
        - fieldnameOne
        - fieldnameTwo
``` 

`collision` can be set to suppress collision checks:

```yaml
field_name:
  - schema.to.skip.one
  - schema.to.skip.two
``` 

The schema can be specified as follow:
- `*` turns off collision check for all schemas
- `schema.to.skip.*` turns off collision check in package `schema.to.skip`. In case `field_name` appears in other schemas contained in a different package, the collision check will then highlight this
- `schema.to.skip.one` turns off the collision check only for the set schema. In case `field_name` appears in other schemas, the collision check will highlight this
