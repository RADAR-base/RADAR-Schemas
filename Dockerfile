FROM gradle:7.0-jdk11 as builder

RUN mkdir -p /code/java-sdk
WORKDIR /code/java-sdk

COPY commons /code/commons
COPY specifications /code/specifications

COPY java-sdk/gradle/*.gradle /code/java-sdk/gradle/
COPY java-sdk/build.gradle java-sdk/settings.gradle /code/java-sdk/
COPY java-sdk/radar-schemas-commons/build.gradle /code/java-sdk/radar-schemas-commons/
COPY java-sdk/radar-schemas-core/build.gradle /code/java-sdk/radar-schemas-core/
COPY java-sdk/radar-schemas-registration/build.gradle /code/java-sdk/radar-schemas-registration/
COPY java-sdk/radar-schemas-tools/build.gradle /code/java-sdk/radar-schemas-tools/
COPY java-sdk/radar-catalog-server/build.gradle /code/java-sdk/radar-catalog-server/
RUN gradle downloadDependencies --no-watch-fs

COPY java-sdk/radar-schemas-commons/src /code/java-sdk/radar-schemas-commons/src
COPY java-sdk/radar-schemas-registration/src /code/java-sdk/radar-schemas-registration/src
COPY java-sdk/radar-schemas-tools/src /code/java-sdk/radar-schemas-tools/src
COPY java-sdk/radar-schemas-core/src /code/java-sdk/radar-schemas-core/src
COPY java-sdk/radar-catalog-server/src /code/java-sdk/radar-catalog-server/src

RUN gradle distTar --no-watch-fs \
  && cd radar-schemas-tools/build/distributions \
  && tar xzf radar-schemas-tools*.tar.gz \
  && cd ../../../radar-catalog-server/build/distributions \
  && tar xzf radar-catalog-server*.tar.gz

FROM openjdk:11-jdk-slim

ENV KAFKA_SCHEMA_REGISTRY=http://schema-registry-1:8081 \
    KAFKA_NUM_PARTITIONS=3 \
    KAFKA_NUM_REPLICATION=3 \
    KAFKA_NUM_BROKERS=3 \
    KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
		rsync \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p /schema/merged /schema/java/src /schema/java/classes /usr/share/java

WORKDIR /schema

RUN curl -#o /usr/share/java/avro-tools.jar \
 "http://archive.apache.org/dist/avro/avro-1.9.2/java/avro-tools-1.9.2.jar"

RUN mkdir original/

COPY --from=builder /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/lib/* /usr/lib/
COPY --from=builder /code/java-sdk/radar-catalog-server/build/distributions/radar-catalog-server-*/lib/* /usr/lib/
COPY --from=builder /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/bin/radar-schemas-tools /usr/bin/
COPY --from=builder /code/java-sdk/radar-catalog-server/build/distributions/radar-catalog-server-*/bin/radar-catalog-server /usr/bin/
COPY ./commons ./original/commons
COPY ./specifications ./original/specifications

VOLUME /schema/conf
VOLUME /etc/confluent/

# Copy bash file
COPY docker/specifications.exclude /etc/radar-schemas/specifications.exclude
COPY docker/topic_init.sh ./docker/init.sh ./docker/cc_topic_init.sh ./docker/list_aggregated.sh ./docker/list_raw.sh /usr/bin/
RUN chmod +x /usr/bin/*.sh

ENTRYPOINT ["init.sh"]
CMD ["radar-schemas-tools", "-h"]
