#!bin/bash

mvn clean install

mkdir -p data/db
mkdir -p data/rabbit
mkdir -p data/flink/chk && mkdir -p data/flink/share && mkdir -p data/flink/uploads && mkdir -p data/flink/svp



