# Apparently there is no jre 17 slim version. :(
# https://github.com/docker-library/openjdk/issues/468
FROM openjdk:17.0.2-jdk-slim

MAINTAINER Isuru Weerarathna <isuruw89@gmail.com>

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.8.0/wait /wait
RUN chmod +x /wait