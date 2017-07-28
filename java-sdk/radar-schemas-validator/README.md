# RADAR Schemas Validator

The RADAR Schemas Validator checks if the Schema Catalog is in a valid state.

It first checks that the folder structure match the following design:
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
  
For each Avro schema under `commons` folder checks:
- in case of `ENUM`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * the is documentation
  * `symbols` match UPPER_CASE format
  * it contains the `UNKNOWN` symbol 
- in case of `RECORD`
  * the `namespace` differs from null and it is a lowercase string dot separated without numeric
  * the `name` matches the .avsc file name and it is an UpperCamelCase string
  * it contains fields
  * the `field name` is a lowerCamelCase string and does not contain string such as `value`, `Value`, `val` and `Val`.
  * schemas under `active` folder have `time` and `timeCompleted`, do not have a field named `timeReceived`
  * schemas under `monitor` folder have `time`, do not have a field named either `timeCompleted` or `timeReceived`
  * schemas under `passive` folder have `time` and `timeReceived`, do not have a field named `timeCompleted`
  * the record and any provided fields are documented
  * `ENUM` fields have `UNKNOWN` as `default` value
  * `nullable`/`optional` fields have `null` as default value 
  
The validation process generates a field name collision summary. It is shown to the end user only in case of collisions.

Upon rule violation, the end user is notfied with a message explaining how to fix the issue.

## How to use

The validation is implemented as a `JUnit` test. To run the validation, simply type `./gradlew test`

## Suppress checks

Record name and field name validations can be suppressed modifying the [skip](src/test/resources/skip.yml) configuration file.

`files` lists files paths that do not need to take into account. All tests are enable by default.

```yaml
schema_to_skip:
  - record_name_check: [ENABLE | DISABLE]
    fields:
      - fieldnameOne
      - fieldnameTwo
``` 

It can contain
- entire path like `commons/active/questionnaire/questionnaire.avsc`
- folder and subfolder `commons/active/**`: all file under `active` and all its subfolder will be skipped
- folder and subfolder `commons/active/**/*.avsc`: all file with format `avsc` under `active` and all its subfolder will be skipped

`validation` allows the user to specify what checks should be skipped at schema level:
- a key like `org.radarcns.passive.biovotion.*` set a skip configuration valid for all schemas under `org.radarcns.passive.biovotion` package
- a key like `org.radarcns.passive.biovotion.BiovotionVSMSpO2` specify a configuration valid only for the given record
- `name_record_check: DISABLE` suppresses the record name check
- `fields` lists all field name for which the field check is suppressed

`collision` can be set to suppress collision checks:

```yaml
field_name:
  - schema.to.skip.one
  - schema.to.skip.two
``` 

The schema can be specified as follow:
- `*` turns off the collision check for all schemas for the given field name
- `schema.to.skip.*` turns off the collision check also for all schemas under the package `schema.to.skip`. In case `field_name` appears in another schemas contained in a different package, the collision check will highlight this
- `schema.to.skip.one` turns off the collision check also for the set schema. In case `field_name` appears in another schemas, the collision check will highlight this  

