#!/bin/bash

set -e

rsync -a /schema/original/commons /schema/original/specifications /schema/merged
rsync -a /schema/conf/ /schema/merged

EXCLUDE_FILE=${EXCLUDE_FILE:-/etc/radar-schemas/specifications.exclude}
if [ -e "$EXCLUDE_FILE" ]; then
  while read -r exclude; do
    rm /schema/merged/specifications/$exclude
  done < "$EXCLUDE_FILE"
fi

if [ $# != 0 ]; then
  exec "$@"
fi
