#!/bin/bash

NUM_TRIES=${TOPIC_INIT_TRIES:-20}

if [ -z NO_VALIDATE ]; then
  radar-schemas-tools validate merged
fi

# Create topics
echo "Creating RADAR-base topics. Will try ${NUM_TRIES} times..."

if radar-schemas-tools create -c "${CONFIG_PATH}" -p $KAFKA_NUM_PARTITIONS -r $KAFKA_NUM_REPLICATION -b $KAFKA_NUM_BROKERS -s "${KAFKA_BOOTSTRAP_SERVERS}" -n ${NUM_TRIES} merged; then
    echo "Created topics"
else
    echo "FAILED TO CREATE TOPICS"
    exit 1
fi

echo "Topics created."

echo "Registering RADAR-base schemas..."
if ! radar-schemas-tools register --force -c "${CONFIG_PATH}" "${KAFKA_SCHEMA_REGISTRY}" merged; then
  echo "FAILED TO REGISTER SCHEMAS"
  exit 1
fi

echo "Schemas registered."

echo "*******************************************"
echo "**  RADAR-base topics and schemas ready   **"
echo "*******************************************"
