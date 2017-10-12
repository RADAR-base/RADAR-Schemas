FROM openjdk:8-jdk-alpine

RUN mkdir /code
WORKDIR /code

COPY gradle /code/gradle
COPY gradle.properties build.gradle settings.gradle  gradlew /code/
RUN ./gradlew tasks
COPY radar-schemas-commons/build.gradle /code/radar-schemas-commons/
COPY radar-schemas-commons/src /code/radar-schemas-commons/src
RUN ./gradlew :radar-schemas-commons:classes
COPY radar-schemas-tools/build.gradle /code/radar-schemas-tools/
COPY radar-schemas-tools/src /code/radar-schemas-tools/src
RUN ./gradlew distTar && cd radar-schemas-tools/build/distributions && tar xzf radar-schemas-tools*.tar.gz

FROM openjdk:8-jre-alpine

COPY --from=0 /code/radar-schemas-tools/build/distributions/radar-schemas-tools-*/lib/* /usr/lib/
COPY --from=0 /code/radar-schemas-tools/build/distributions/radar-schemas-tools-*/bin/radar-schemas-tools /usr/bin/

WORKDIR /schemas
VOLUME /schemas

CMD ["radar-schemas-tools", "-h"]
