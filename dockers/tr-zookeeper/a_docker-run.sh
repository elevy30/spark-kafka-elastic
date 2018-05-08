#!/bin/bash
docker run --restart=unless-stopped  -d --name zookeeper-local --hostname zookeeper-local -p 2181:2181 -e ZOO_MY_ID=1 -e ZOO_SERVERS='server.1=zookeeper-local:2888:3888' zookeeper-1





#--mount type=bind,source=/Data/thetaray/kafka,destination=/Data/thetaray/kafka 

 
