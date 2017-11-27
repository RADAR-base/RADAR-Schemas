FROM openjdk:8-jdk-alpine

RUN mkdir -p /code/java-sdk
WORKDIR /code/java-sdk

COPY commons /code/commons
COPY specifications /code/specifications
COPY restapi /code/restapi
COPY java-sdk/gradle /code/java-sdk/gradle
COPY java-sdk/build.gradle java-sdk/settings.gradle  java-sdk/gradlew /code/java-sdk/
RUN ./gradlew tasks
COPY java-sdk/radar-schemas-commons/build.gradle /code/java-sdk/radar-schemas-commons/
COPY java-sdk/radar-schemas-commons/src /code/java-sdk/radar-schemas-commons/src
RUN ./gradlew :radar-schemas-commons:classes
COPY java-sdk/radar-schemas-tools/build.gradle /code/java-sdk/radar-schemas-tools/
COPY java-sdk/radar-schemas-tools/src /code/java-sdk/radar-schemas-tools/src
RUN ./gradlew distTar && cd radar-schemas-tools/build/distributions && tar xzf radar-schemas-tools*.tar.gz

FROM openjdk:8-jre-alpine

COPY --from=0 /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/lib/* /usr/lib/
COPY --from=0 /code/java-sdk/radar-schemas-tools/build/distributions/radar-schemas-tools-*/bin/radar-schemas-tools /usr/bin/

WORKDIR /schemas
VOLUME /schemas

CMD ["radar-schemas-tools", "-h"]
