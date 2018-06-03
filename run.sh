#!/bin/bash

# this script will get and start kafka, zk, events generator.
#   it will redirect events into kafka topic bpevents and launch the server
#   it does not validate anything in addition zookeeper and kafka will remain
#   up and running until killed manually
#   - sbt and java should be installed and JAVA_HOME defined

export KAFKA_VER=kafka_2.12-1.1.0

function generate_events_into_kafka() {
    ./generator-linux-amd64 | $KAFKA_VER/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic bpevents
}

[ -d workdir ] | mkdir workdir

if ! [ -f generator-linux-amd64 ]; then
    echo "getting events generator"
    wget https://s3-us-west-1.amazonaws.com/bp-interview-artifacts/generator-linux-amd64
    chmod +x generator-linux-amd64
fi

if ! [ -d $KAFKA_VER ]; then
    echo "getting kafka"
    wget https://apache.mivzakim.net/kafka/1.1.0/$KAFKA_VER.tgz
    validateAction $?
    tar -xzf $KAFKA_VER.tgz
fi

cd $KAFKA_VER
echo "starting zookeeper"
bin/zookeeper-server-start.sh config/zookeeper.properties &> ../workdir/zk.out &
sleep 10
echo "starting kafka" 
bin/kafka-server-start.sh config/server.properties &> ../workdir/kafka.out &
sleep 10
echo "create bpevents topic"
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic bpevents
cd ..    

generate_events_into_kafka &> workdir/generator.out &

echo "starting server"
cd akka-http
sbt "run localhost:9092 bpevents"
