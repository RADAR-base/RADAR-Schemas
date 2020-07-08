#!/bin/bash

# Create topics
echo "Creating RADAR-base topics on Confluent Cloud..."

if ! radar-schemas-tools cc-topic-create -c $CC_CONFIG_FILE_PATH -p $KAFKA_NUM_PARTITIONS -r $KAFKA_NUM_REPLICATION  merged; then
  echo "FAILED TO CREATE TOPICS ... Retrying again"
  if ! radar-schemas-tools cc-topic-create -c $CC_CONFIG_FILE_PATH -p $KAFKA_NUM_PARTITIONS -r $KAFKA_NUM_REPLICATION  merged; then
    echo "FAILED TO CREATE TOPICS"
    exit 1
  else
    echo "Created topics at second attempt"
  fi
else
  echo "Topics created."
fi

echo "Registering RADAR-base schemas..."

if ! radar-schemas-tools register --force -u $CC_API_KEY -p $CC_API_SECRET "${KAFKA_SCHEMA_REGISTRY}" merged; then
  echo "FAILED TO REGISTER SCHEMAS"
  exit 1
fi

echo "Schemas registered."

echo "*******************************************"
echo "**  RADAR-base topics and schemas ready   **"
echo "*******************************************"
