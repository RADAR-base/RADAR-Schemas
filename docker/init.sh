#!/bin/bash

set -e

AVRO_TOOLS=/usr/share/java/avro-tools.jar

rsync -a /schema/original/commons /schema/original/specifications /schema/merged
rsync -a /schema/conf/ /schema/merged

EXCLUDE_FILE=${EXCLUDE_FILE:-/etc/radar-schemas/specifications.exclude}
if [ -f "$EXCLUDE_FILE" ]; then
  while read -r exclude; do
    rm /schema/merged/specifications/$exclude
  done < "$EXCLUDE_FILE"
fi

if [ $# != 0 ]; then
  exec "$@"
fi
