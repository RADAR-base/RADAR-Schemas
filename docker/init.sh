#!/bin/bash

set -e

rsync -a /schema/original/commons /schema/original/specifications /schema/merged
rsync -a /schema/conf/ /schema/merged

if [ $# != 0 ]; then
  exec "$@"
fi
