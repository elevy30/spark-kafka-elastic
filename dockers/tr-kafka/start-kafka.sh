#!/bin/bash

create-topics.sh &

if [[ -n "$CUSTOM_INIT_SCRIPT" ]] ; then
  eval $CUSTOM_INIT_SCRIPT
fi

exec $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties