FROM docker.elastic.co/elasticsearch/elasticsearch:6.0.0

RUN chown -R elasticsearch:elasticsearch /usr/share/elasticsearch/data

HEALTHCHECK --interval=2m CMD pgrep java || Exit 1
