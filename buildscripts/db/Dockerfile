FROM mysql:8
MAINTAINER Isuru Weerarathna <uisurumadushanka89@gmail.com>

ENV MYSQL_DATABASE=oasis MYSQL_ROOT_PASSWORD=root MYSQL_USER=oasis MYSQL_PASSWORD=oasis

ARG SCHEMA_PATH
ADD ${SCHEMA_PATH} /docker-entrypoint-initdb.d

EXPOSE 3306
