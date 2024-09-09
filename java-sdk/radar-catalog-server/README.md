# Flow

Below are several sequence diagrams illustrating the flow of the application during startup and regular operation.

## Startup

On startup, an init container (kafka-init) will be launched to add configured topics to the Kafka schema registry.

This is done using the [docker/topic_init.sh](../../docker/topic_init.sh) script, which calls the `radar-schemas-tools` 
command line app

```mermaid
sequenceDiagram
    participant kafka-init as kafka-init
    participant kafka as Kafka
    participant schema-registry as Schema Registry

    kafka-init ->> kafka: Create topics
    kafka-init ->> schema-registry: Register schemas
```
## Regular operation

After startup the `catalog-server` application will be running and listening for requests on public endpoints. 
This application is responsible for providing the source-types on requests, which are configured in the [specifications](../../specifications) folder.

```mermaid
sequenceDiagram
    participant client as Client
    participant catalog_server as Catalog Server

    client ->> catalog_server: Request source-types
    catalog_server ->> catalog_server: Read source-types from /specifications/
    catalog_server ->> client: Return source-types
```
