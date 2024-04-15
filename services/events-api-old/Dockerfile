FROM oasis/base-java

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

WORKDIR /usr/share/oasis-events-api
CMD /wait && exec java -classpath ./*:libs/* io.github.oasis.services.events.Main
#ENTRYPOINT ["java", "-classpath", "./*:libs/*", "io.github.oasis.services.events.Main"]
EXPOSE 8050

RUN mkdir -p /etc/oasis
ENV OASIS_CONFIG_FILE "/etc/oasis/events-api.yml"

# Add dependency library files
ADD target/libs /usr/share/oasis-events-api

# Add main executing jar
ADD target/oasis-events-api.jar /usr/share/oasis-events-api/oasis-events-api.jar
