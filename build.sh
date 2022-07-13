#!bin/bash

echo "==============================================================================="
echo "Building Oasis..."
echo "==============================================================================="
mvn install -DskipTests

cp externals/kafka-stream/target/libs/* buildscripts/modules
cp externals/kafka-stream/target/oasis-ext-kafkastream-1.0-SNAPSHOT.jar buildscripts/modules

echo "==============================================================================="
echo "Building Events API Docker Image..."
echo "==============================================================================="
cd services/events-api
#docker build -t oasis/events-api .

cd ../..

echo "==============================================================================="
echo "Building Admin/Stats API Docker Image..."
echo "==============================================================================="
cd services/stats-api
#docker build -t oasis/stats-api .

cd ../..

echo "==============================================================================="
echo "Building Feeder Docker Image..."
echo "==============================================================================="
cd services/feeder
docker build -t oasis/feeder .

cd ../..

echo "==============================================================================="
echo "Building Engine Docker Image..."
echo "==============================================================================="
cd engine
#docker build -t oasis/engine .

cd ..

mkdir -p .tmpdata/enginedb
mkdir -p .tmpdata/cache


echo "==============================================================================="
echo "Starting Oasis..."
echo "==============================================================================="
docker-compose up



