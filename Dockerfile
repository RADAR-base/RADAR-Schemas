FROM gradle:7.2-jdk17 as builder

RUN mkdir -p /code/java-sdk
WORKDIR /code/java-sdk
ENV GRADLE_USER_HOME=/code/.gradlecache

COPY java-sdk/build.gradle java-sdk/settings.gradle /code/java-sdk/
COPY java-sdk/radar-schemas-commons/build.gradle /code/java-sdk/radar-schemas-commons/
COPY java-sdk/radar-schemas-core/build.gradle /code/java-sdk/radar-schemas-core/
COPY java-sdk/radar-schemas-registration/build.gradle /code/java-sdk/radar-schemas-registration/
COPY java-sdk/radar-schemas-tools/build.gradle /code/java-sdk/radar-schemas-tools/
COPY java-sdk/radar-catalog-server/build.gradle /code/java-sdk/radar-catalog-server/
RUN gradle downloadDependencies --no-watch-fs -Pprofile=docker

COPY commons /code/commons
COPY specifications /code/specifications

COPY java-sdk/radar-schemas-commons/src /code/java-sdk/radar-schemas-commons/src
COPY java-sdk/radar-schemas-core/src /code/java-sdk/radar-schemas-core/src
COPY java-sdk/radar-schemas-registration/src /code/java-sdk/radar-schemas-registration/src
COPY java-sdk/radar-schemas-tools/src /code/java-sdk/radar-schemas-tools/src
COPY java-sdk/radar-catalog-server/src /code/java-sdk/radar-catalog-server/src

RUN gradle distTar --no-watch-fs -Pprofile=docker \
  && cd radar-schemas-tools/build/distributions \
  && tar xzf radar-schemas-tools*.tar.gz \
  && cd ../../../radar-catalog-server/build/distributions \
  && tar xzf radar-catalog-server*.tar.gz

FROM azul/zulu-openjdk-alpine:17-jre-headless

ENV KAFKA_SCHEMA_REGISTRY=http://schema-registry-1:8081 \
    SCHEMA_REGISTRY_API_KEY="" \
    SCHEMA_REGISTRY_API_SECRET="" \
    KAFKA_NUM_PARTITIONS=3 \
    KAFKA_NUM_REPLICATION=3 \
    KAFKA_NUM_BROKERS=3 \
    KAFKA_BOOTSTRAP_SERVERS="" \
    KAFKA_CONFIG_PATH="" \
    NO_VALIDATE=""

RUN apk add --no-cache \
		curl \
		rsync

WORKDIR /schema

RUN mkdir -p original merged java/src java/classes /usr/share/java \
  && chown 101 merged java/src java/classes

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
COPY docker/topic_init.sh ./docker/init.sh ./docker/list_aggregated.sh ./docker/list_raw.sh /usr/bin/

USER 101

ENTRYPOINT ["init.sh"]
CMD ["radar-schemas-tools", "-h"]
