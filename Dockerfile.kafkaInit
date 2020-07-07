FROM openjdk:11-jdk-slim as builder

RUN mkdir -p /code/java-sdk
WORKDIR /code/java-sdk

COPY ./commons /code/commons
COPY ./specifications /code/specifications
COPY ./java-sdk/gradle /code/java-sdk/gradle
COPY ./java-sdk/build.gradle /code/java-sdk/build.gradle
COPY ./java-sdk/settings.gradle java-sdk/gradlew /code/java-sdk/

ENV GRADLE_OPTS -Dorg.gradle.daemon=false

RUN ./gradlew tasks
COPY ./java-sdk/radar-schemas-commons/build.gradle /code/java-sdk/radar-schemas-commons/
COPY ./java-sdk/radar-schemas-commons/src /code/java-sdk/radar-schemas-commons/src
RUN ./gradlew :radar-schemas-commons:jar
COPY ./java-sdk/radar-schemas-tools/build.gradle /code/java-sdk/radar-schemas-tools/
COPY ./java-sdk/radar-schemas-tools/src /code/java-sdk/radar-schemas-tools/src
RUN ./gradlew distTar && cd radar-schemas-tools/build/distributions && tar xzf radar-schemas-tools*.tar.gz


FROM openjdk:11-jdk-slim

ENV KAFKA_SCHEMA_REGISTRY=http://schema-registry-1:8081
ENV KAFKA_NUM_PARTITIONS=3
ENV KAFKA_NUM_REPLICATION=3
ENV KAFKA_NUM_BROKERS=3
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper-1:2181

RUN apt-get update && apt-get install -y --no-install-recommends \
		curl \
		rsync \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p /schema/merged /schema/java/src /schema/java/classes /usr/share/java

WORKDIR /schema

RUN curl -#o /usr/share/java/avro-tools.jar \
 "http://archive.apache.org/dist/avro/avro-1.8.2/java/avro-tools-1.8.2.jar"

RUN mkdir original/

COPY --from=builder /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/lib/* /usr/lib/
COPY --from=builder /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/bin/radar-schemas-tools /usr/bin/
COPY --from=builder /code ./original

VOLUME /schema/conf
# Copy bash file
COPY ./docker/topic_init.sh ./docker/init.sh ./docker/list_aggregated.sh ./docker/list_raw.sh /usr/bin/
RUN chmod +x /usr/bin/*.sh

ENTRYPOINT ["init.sh"]
CMD ["topic_init.sh"]
