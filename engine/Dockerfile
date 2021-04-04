FROM openjdk:11.0-jre-slim

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.8.0/wait /wait
RUN chmod +x /wait

WORKDIR /usr/share/oasis-engine
CMD /wait && exec java -classpath ./*:libs/*:/etc/oasis/modules/* io.github.oasis.engine.OasisEngineRunner

RUN mkdir -p /etc/oasis
ENV ENGINE_CONFIG_FILE "/etc/oasis/engine.conf"

# Add dependency library files
ADD target/libs /usr/share/oasis-engine

# Add main executing jar
ADD target/oasis-engine.jar /usr/share/oasis-engine/oasis-engine.jar
