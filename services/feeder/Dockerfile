FROM openjdk:11.0-jre-slim

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>
## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.8.0/wait /wait
RUN chmod +x /wait

WORKDIR /usr/share/oasis-feeder
CMD /wait && exec java -classpath ./*:libs/*:/etc/oasis/modules/* io.github.oasis.services.feeds.Feeder

RUN mkdir -p /etc/oasis
ENV OASIS_CONFIG_FILE "/etc/oasis/feeder.conf"

# Add dependency library files
ADD target/libs /usr/share/oasis-feeder

# Add main executing jar
ADD target/oasis-feeder.jar /usr/share/oasis-feeder/oasis-feeder.jar
