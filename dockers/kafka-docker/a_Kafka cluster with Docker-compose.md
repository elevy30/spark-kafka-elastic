http://flylabs.blogspot.co.il/2016/11/kafka-cluster-docker-compose-solution.html

```javascript
Help with docker-compose:
docker-compose down
docker-compose build
docker-compose up -d
```

```javascript
Kafka cluster with Docker-compose
For a single VM/workstation I decided to implement Docker/docker-compose solution, such as Multi-Broker Apache Kafka from wurstmeister/kafka. I used the Linux Ubuntu 16.04 with installed Docker 1.12.1 and Docker-compose 1.8.1.
```

Steps:

```javascript
1.     First of all we should clone kafka-docker repository

git clone https://github.com/wurstmeister/kafka-docker.git
```

```javascript
2.     Content of the directory:

root@srv3:/opt/kafka-docker# ll
total 72  
drwxr-xr-x 3 root root  4096 Nov 18 20:51 ./ 
drwxr-xr-x 5 root root  4096 Nov 18 19:32 ../ 
-rwxr-xr-x 1 root root   232 Nov 18 20:51 broker-list.sh* 
-rwxr-xr-x 1 root root   911 Nov 18 19:32 create-topics.sh* 
-rw-r--r-- 1 root root   367 Nov 18 19:32 docker-compose-single-broker.yml 
-rw-r--r-- 1 root root   323 Nov 18 20:33 docker-compose.yml 
-rw-r--r-- 1 root root   883 Nov 18 19:32 Dockerfile 
-rwxr-xr-x 1 root root   269 Nov 18 19:32 download-kafka.sh* 
drwxr-xr-x 8 root root  4096 Nov 18 19:32 .git/ 
-rw-r--r-- 1 root root 11325 Nov 18 19:32 LICENSE 
-rw-r--r-- 1 root root  3646 Nov 18 19:32 README.md 
-rwxr-xr-x 1 root root  2035 Nov 18 19:32 start-kafka.sh* 
-rwxr-xr-x 1 root root   131 Nov 18 19:32 start-kafka-shell.sh* 

```

```javascript
3.     During the process of deploying and testing I have fixed the bug in the script broker-list.sh. It was changed variable from $HOST_IP to $KAFKA_ADVERTISED_HOST_NAME.
sed -i 's/$HOST_IP:/$KAFKA_ADVERTISED_HOST_NAME:/g' broker-list.sh

Due to this bug, running of  Kafka Producer could not be executed by command:

$KAFKA_HOME/bin/kafka-console-producer.sh \
--topic=topic-InfobipTask \
--broker-list=`broker-list.sh`
```

```javascript
4.     Before running the cluster, in file docker-compose.yml should change value of variable KAFKA_ADVERTISED_HOST_NAME on actual host IP address.

root@srv3:/opt/kafka-docker# cat docker-compose.yml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    build: .
    ports:
      - "9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 192.168.0.101
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

```javascript
5.     Run Kafka cluster in daemon mode inside the directory.

docker-compose up –d
```

```javascript
6.     To add two nodes without disrupting of the cluster, I executed the command:

docker-compose scale kafka=3

     # Show status of cluster:
docker-compose ps
Name                   Command             State    Ports
---------------------------------------------------------------------------------
kafkadocker_kafka_1    start-kafka.sh       Up      0.0.0.0:32777->9092/tcp
kafkadocker_kafka_2    start-kafka.sh       Up      0.0.0.0:32779->9092/tcp
kafkadocker_kafka_3    start-kafka.sh       Up      0.0.0.0:32778->9092/tcp
kafkadocker_zookeeper_1 /bin/sh -c /usr/sbin/sshd  ...  Up .0.0.0:2181->2181/tcp, 22/tcp, 2888/tcp, 3888/tcp
```

```javascript
7.     For access to shell of one of the brokers I used command:

docker exec -it kafkadocker_kafka_1 /bin/bash
```

```javascript
8.     In file controller.log we can see that connecting all nodes to each other is successful.

bash-4.3# tail -f /opt/kafka_2.11-0.10.1.0/logs/controller.log
[2016-11-18 18:12:09,246] DEBUG [Channel manager on controller 1001]: Controller 1001 trying to connect to broker 1002 (kafka.controller.ControllerChannelManager)
[2016-11-18 18:12:09,266] INFO [Controller 1001]: New broker startup callback for 1002 (kafka.controller.KafkaController)
[2016-11-18 18:12:09,270] INFO [Controller-1001-to-broker-1002-send-thread], Starting  (kafka.controller.RequestSendThread)
[2016-11-18 18:12:09,317] INFO [Controller-1001-to-broker-1002-send-thread], Controller 1001 connected to 192.168.0.101:32778 (id: 1002 rack: null) for sending state change requests (kafka.controller.RequestSendThread)
[2016-11-18 18:12:41,690] INFO [BrokerChangeListener on Controller 1001]: Broker change listener fired for path /brokers/ids with children 1001,1002,1003 (kafka.controller.ReplicaStateMachine$BrokerChangeListener)
[2016-11-18 18:12:41,743] INFO [BrokerChangeListener on Controller 1001]: Newly added brokers: 1003, deleted brokers: , all live brokers: 1001,1002,1003 (kafka.controller.ReplicaStateMachine$BrokerChangeListener)
[2016-11-18 18:12:41,743] DEBUG [Channel manager on controller 1001]: Controller 1001 trying to connect to broker 1003 (kafka.controller.ControllerChannelManager)
[2016-11-18 18:12:41,782] INFO [Controller 1001]: New broker startup callback for 1003 (kafka.controller.KafkaController)
[2016-11-18 18:12:41,787] INFO [Controller-1001-to-broker-1003-send-thread], Starting  (kafka.controller.RequestSendThread)
[2016-11-18 18:12:41,796] INFO [Controller-1001-to-broker-1003-send-thread], Controller 1001 connected to 192.168.0.101:32779 (id: 1003 rack: null) for sending state change requests (kafka.controller.RequestSendThread)
```

```javascript
9.  Create a topic

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper zookeeper:2181 \
--create --topic topic-InfobipTask \
--partitions 3 --replication-factor 3
Created topic "topic-InfobipTask".
```

```javascript
10. List of topics

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper zookeeper:2181 –-list
topic-InfobipTask
```

```javascript
11.  Describe topic

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper zookeeper:2181 \
--describe --topic topic-InfobipTask
Topic:topic-InfobipTask PartitionCount:3  ReplicationFactor:3 Configs:
Topic: topic-InfobipTask        Partition: 0    Leader: 1002    Replicas: 1002,1003,1001        Isr: 1002,1001,1003
Topic: topic-InfobipTask        Partition: 1    Leader: 1002    Replicas: 1003,1001,1002        Isr: 1002,1001,1003
Topic: topic-InfobipTask        Partition: 2    Leader: 1002    Replicas: 1001,1002,1003        Isr: 1002,1001,1003
```

```javascript
12. Start producer in the broker kafkadocker_kafka_1:

docker exec -it kafkadocker_kafka_1 /bin/bash
bash-4.3# $KAFKA_HOME/bin/kafka-console-producer.sh \
> --broker-list=`broker-list.sh` \
> --topic topic-InfobipTask
Hello,
My name is Sergey Shimanskiy,
Could you please consider my CV for position DevOps Engineer,
Thank you.
My contact +7-911-989-49-77
```

```javascript
13. Also I launched consumer in separate terminal for kafkadocker_kafka_3 and received the same  message:

docker exec -it kafkadocker_kafka_3 /bin/bash
$KAFKA_HOME/bin/kafka-console-consumer.sh \
--zookeeper=zookeeper:2181 \
--topic topic-InfobipTask
Using the ConsoleConsumer with old consumer is deprecated and will be removed in a future major release. Consider using the new consumer by passing [bootstrap-server] instead of [zookeeper].
Hello,
My name is Sergey Shimanskiy,
Could you please consider my CV for position DevOps Engineer,
Thank you.
My contact +7-911-989-49-77
```

```javascript
14.  Then I removed broker kafkadocker_kafka_3 from the cluster using command docker-compose scale kafka=2 and continued to receive messages by connecting to kafkadocker_kafka_2 via separated terminal.

bash-4.3# $KAFKA_HOME/bin/kafka-console-consumer.sh \
> --zookeeper=zookeeper:2181 \
> --topic topic-InfobipTask
Using the ConsoleConsumer with old consumer is deprecated and will be removed in a futurthe new consumer by passing [bootstrap-server] instead of [zookeeper].
Hi there!
Anybody home?!
```

```javascript
15.   As we can see all works fine.

Notice:  For RHEL/Centos 7 we should open appropriate port range in firewall:
firewall-cmd --zone=public --add-port=32700-32800/tcp --permanent
success
firewall-cmd –reload
```

```javascript
16.   Also for a quick running the cluster we can use script kafka.sh:

#!/bin/bash
# Please execute the script with specifying your host IP address, not 127.0.0.1
# For example: ./kafka.sh 192.168.0.101
# Before executing, Docker and docker-compose of last version should be installed
# For RHEL/Centos 7 please open port range. Uncomment if needed:
# firewall-cmd --zone=public --add-port=31000-33000/tcp --permanent
# firewall-cmd --reload
#
hostip="$1"
# Clone repository
git clone https://github.com/wurstmeister/kafka-docker.git /kafka-docker
cd /kafka-docker
# Changing host ip
sed -i 's/192.168.99.100/'$hostip'/g' docker-compose.yml
# Changing variables
sed -i 's/$HOST_IP:/$KAFKA_ADVERTISED_HOST_NAME:/g' broker-list.sh
# Running the cluster
docker-compose up -d
echo "Done"

Thank you.
```