#!bin/bash

mvn install

cd target

java -Dgame.data.dir=../stackoverflow -classpath ./*:libs/* io.github.oasis.simulations.Main api

cd ..