FROM rabbitmq:3-management
MAINTAINER Isuru Weerarathna <uisurumadushanka89@gmail.com>

ADD rabbitmq.config /etc/rabbitmq/
ADD definitions.json /etc/rabbitmq/

RUN chown rabbitmq:rabbitmq /etc/rabbitmq/rabbitmq.config /etc/rabbitmq/definitions.json
CMD ["rabbitmq-server"]