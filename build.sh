#!bin/bash

mvn clean install

docker build -f buildscripts/Dockerfile-db -t uisuru89/oasis-db .
docker build -f buildscripts/Dockerfile-rabbit -t uisuru89/oasis-rabbit .

docker build -f buildscripts/Dockerfile-services -t uisuru89/oasis-services .
docker build -f buildscripts/Dockerfile-injector -t uisuru89/oasis-injector .

