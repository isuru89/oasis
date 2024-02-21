FROM oasis/base-java

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

WORKDIR /usr/share/oasis-engine
CMD /wait && exec java -classpath ./*:libs/*:/etc/oasis/modules/* io.github.oasis.engine.OasisEngineRunner

RUN mkdir -p /etc/oasis
ENV ENGINE_CONFIG_FILE "/etc/oasis/engine.yml"

# Add dependency library files
ADD target/libs /usr/share/oasis-engine

# Add main executing jar
ADD target/oasis-engine.jar /usr/share/oasis-engine/oasis-engine.jar
