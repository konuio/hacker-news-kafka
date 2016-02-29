Hacker News Kafka
===
How to run
---
1. Open HackerNewsReader. Right click and debug in IntelliJ.

Starting Kafka and Zookeeper
---
```
brew install zookeeper
brew install kafka

zookeeper-server-start.sh zookeeper/zookeeper.properties
zookeeper-server-stop.sh zookeeper/zookeeper.properties

kafka-server-start.sh kafka/kafka.properties
kafka-server-stop.sh kafka/kafka.properties
```

Sending a test message
---
```
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
kafka-topics.sh --list --zookeeper localhost:2181

kafka-console-producer.sh --broker-list localhost:9091 --topic test
kafka-console-consumer.sh --zookeeper localhost:2181 --topic test
```
