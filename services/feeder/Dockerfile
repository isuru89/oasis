FROM oasis/base-java

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

WORKDIR /usr/share/oasis-feeder
CMD /wait && exec java -classpath ./*:libs/*:/etc/oasis/modules/* io.github.oasis.services.feeds.Feeder

RUN mkdir -p /etc/oasis
ENV OASIS_CONFIG_FILE "/etc/oasis/feeder.yml"

# Add dependency library files
ADD target/libs /usr/share/oasis-feeder

# Add main executing jar
ADD target/oasis-feeder.jar /usr/share/oasis-feeder/oasis-feeder.jar
