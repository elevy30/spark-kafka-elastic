#!/bin/bash


# set -e

# echo "Getting Zookeeper settings"
# #Check Postgres availability
# SERVER=$(echo "$KAFKA_ZOOKEEPER_CONNECT" | awk -F "," '{print $1}' | awk -F ":" '{print $1}')
# PORT=$(echo "$KAFKA_ZOOKEEPER_CONNECT" | awk -F "," '{print $1}' | awk -F ":" '{print $2}')
# echo "Server: $SERVER and port: $PORT"

# `nc -z -v -w5 $SERVER $PORT &> /dev/null`
# result1=$?
# retry_count=5
# sleep_time=20
# while [  "$result1" != 0 ] && [ "$retry_count" -gt 0 ]
# do
# 	echo  "port $PORT on $SERVER is closed. trying again"
# 	sleep $sleep_time
# 	`nc -z -v -w5 $SERVER $PORT &> /dev/null`
# 	result1=$?
# 	let retry_count=retry_count-1
# done
# if [  "$result1" != 0 ]
# then
# 	echo "Could not reach $PORT on $SERVER. Quitting application"
# 	exit -1
# fi
# echo "Port $PORT on $SERVER is open!!"

# echo "Starting Kafka"
start-kafka.sh
