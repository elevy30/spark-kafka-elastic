FROM openjdk:8-jre-alpine

ARG kafka_version=1.0.0
ARG scala_version=2.12

MAINTAINER wurstmeister

RUN apk add --update unzip wget curl docker jq coreutils tar gzip bash

ENV KAFKA_VERSION=$kafka_version SCALA_VERSION=$scala_version
ADD download-kafka.sh /tmp/download-kafka.sh
RUN mkdir /opt && chmod a+x /tmp/download-kafka.sh && sync && /tmp/download-kafka.sh && tar xfz /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz -C /opt && rm /tmp/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && ln -s /opt/kafka_${SCALA_VERSION}-${KAFKA_VERSION} /opt/kafka

VOLUME ["/kafka"]

ENV KAFKA_HOME /opt/kafka
ENV PATH ${PATH}:${KAFKA_HOME}/bin
ADD start-kafka.sh /usr/bin/start-kafka.sh
ADD broker-list.sh /usr/bin/broker-list.sh
ADD create-topics.sh /usr/bin/create-topics.sh
ADD server.properties /opt/kafka/config/server.properties
ADD docker-entrypoint.sh /
# The scripts need to have executable permission
RUN chmod a+x /usr/bin/start-kafka.sh && \
    chmod a+x /usr/bin/broker-list.sh && \
    chmod a+x /usr/bin/create-topics.sh && \
    chmod a+x /docker-entrypoint.sh

HEALTHCHECK --interval=2m CMD pgrep java || Exit 1

# Use "exec" form so that it runs as PID 1 (useful for graceful shutdown)
ENTRYPOINT ["/docker-entrypoint.sh"]
