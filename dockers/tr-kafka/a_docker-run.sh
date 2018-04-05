#!/bin/bash
docker run --restart=unless-stopped -d --name kafka-local --hostname kafka-local -e HOSTNAME_COMMAND=hostname -e KAFKA_ZOOKEEPER_CONNECT='zookeeper-local:2181' -e KAFKA_BROKER_ID=1 -e KAFKA_PORT=9094 -e KAFKA_CREATE_TOPICS='temp_topic:1:1' -p 9094:9094  kafka-1




#--mount type=bind,source=/Data/thetaray/kafka,destination=/Data/thetaray/kafka 

 
