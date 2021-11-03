FROM openjdk:11.0-jre-slim

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.8.0/wait /wait
RUN chmod +x /wait

WORKDIR /usr/share/oasis-stats-api
CMD /wait && exec java -classpath ./*:libs/*:/etc/oasis/modules/* io.github.oasis.core.services.api.StatsApiApplication --spring.config.location=file:/etc/oasis/stats-api.properties
EXPOSE 8010

RUN mkdir -p /etc/oasis/schema
ENV OASIS_CONFIG_FILE "/etc/oasis/stats-api.conf"
ADD src/main/resources/io/github/oasis/db/schema /etc/oasis/schema
RUN chmod -R 777 /etc/oasis

# Add dependency library files
ADD target/libs /usr/share/oasis-stats-api

# Add main executing jar
ADD target/oasis-stats-api.jar /usr/share/oasis-stats-api/oasis-stats-api.jar
