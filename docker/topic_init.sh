#!/bin/bash

if [ -z NO_VALIDATE ]; then
  radar-schemas-tools validate merged
fi

# Create topics
echo "Creating RADAR-base topics..."

if ! radar-schemas-tools create -c "${KAFKA_CONFIG_PATH}" -p $KAFKA_NUM_PARTITIONS -r $KAFKA_NUM_REPLICATION -b $KAFKA_NUM_BROKERS -s "${KAFKA_BOOTSTRAP_SERVERS}" merged; then
  echo "FAILED TO CREATE TOPICS ... Retrying"
  if ! radar-schemas-tools create -c "${KAFKA_CONFIG_PATH}" -p $KAFKA_NUM_PARTITIONS -r $KAFKA_NUM_REPLICATION -b $KAFKA_NUM_BROKERS -s "${KAFKA_BOOTSTRAP_SERVERS}" merged; then
    echo "FAILED TO CREATE TOPICS"
    exit 1
  else
    echo "Created topics at second attempt"
  fi
else
  echo "Topics created."
fi

echo "Registering RADAR-base schemas..."
if ! radar-schemas-tools register --force -u "$SCHEMA_REGISTRY_API_KEY" -p "$SCHEMA_REGISTRY_API_SECRET" "${KAFKA_SCHEMA_REGISTRY}" merged; then
  echo "FAILED TO REGISTER SCHEMAS"
  exit 1
fi

echo "Schemas registered."

echo "*******************************************"
echo "**  RADAR-base topics and schemas ready   **"
echo "*******************************************"
