FROM openjdk:8-jre
MAINTAINER Isuru Weerarathna <uisurumadushanka89@gmail.com>

WORKDIR /usr/share/oasis
ENTRYPOINT ["/usr/bin/java", "-classpath", ".:libs/*", "io.github.isuru.oasis.services.OasisServer"]
EXPOSE 5885

ARG JAR_FILE
ARG CONFIG_DIR

# Add dependency library files
ADD target/libs /usr/share/oasis

# Add main executing jar
ADD target/${JAR_FILE} /usr/share/oasis/oasis-service.jar


