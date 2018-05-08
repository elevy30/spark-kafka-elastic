#!/bin/bash
name=$1
version=$2


fullimagename=${name}-${version}
echo "Building docker image -> this may take few mins ${fullimagename}"
IMG=`docker build -t ${fullimagename} .`
