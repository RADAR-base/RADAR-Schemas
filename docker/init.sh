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

# Compiling updated schemas
echo "Compiling schemas..." >&2

# Regex for schemas with a dependency that is a class
# e.g., a literal class starting with a capital, or
# a namespace with internal periods.
DEPENDENT_REGEX='"(items|type)": (\[\s*"null",\s*)?"([A-Z]|[^".]*\.)'

# Separate enums so that they can be referenced in later files
find merged/commons -name "*.avsc" -print | sort > merged/file_list
DEPENDENT=$(find merged/commons -name "*.avsc" -exec grep -Eq "$DEPENDENT_REGEX" "{}" \; -print | sort)
# remove dependent files from all files to get independent files
INDEPENDENT=$(printf "${DEPENDENT}" | comm -23 merged/file_list -)

printf "===> Independent schemas:\n${INDEPENDENT}\n"
printf "===> Dependent schemas:\n${DEPENDENT}\n"

java -jar "${AVRO_TOOLS}" compile -string schema ${INDEPENDENT} ${DEPENDENT} java/src 2>/dev/null
find java/src -name "*.java" -print0 | xargs -0 javac -cp /usr/lib/*:java/classes -d java/classes -sourcepath java/src
# Update the radar schemas so the tools find the new classes in classpath
jar uf /usr/lib/radar-schemas-commons-*.jar -C java/classes .

if [ $# != 0 ]; then
  exec "$@"
fi
