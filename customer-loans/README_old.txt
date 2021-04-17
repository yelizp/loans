The project intends to solve the use case described as:

When a loan is utilized by a customer, loan and account records are created. Given these two unbounded sources (Loan and Account) implement a streaming(Spark, Flink or any other one)  application that calculates the total number of loans, total amount of loans and loans created on last minute per account type.
- Assume that maximum latency between the loan and account records is 30 seconds. Records are created at most in 30 second gap.
- Output must be calculated for each minute.
- Total amount and count must be calculated from application start. At the application start count and amount should be 0.
- Last minute count must contain the number of Loans created for the previous minute.

There are two modules:
1. customer-loans
2. kafka-record-gen
   Produces synthetic Account and Loan records to Kafka. You may use run.bat file to run the module.

Requirements:
1. Download and install Confluent docker image as described by:
   https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html#ce-docker-quickstart

   Once setup, you can access the Confluent Control Center via: http://localhost:9021

   Or:
   docker pull confluentinc/cp-kafka
   docker pull confluentinc/cp-zookeeper
   docker run -d --name=zookeeper -p 2181:2181 -e ZOOKEEPER_CLIENT_PORT=2181 -e ZOOKEEPER_TICK_TIME=2000 -e ZOOKEEPER_SYNC_LIMIT=2 confluentinc/cp-zookeeper
   docker run -d --name=kafka -p 9092:9092 -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_BROKER_ID=2 confluentinc/cp-kafka

2. Requires Spark 3.1.1 for Hadoop 3.2

Notes: I have put a great deal of effort to read Avro message from Kafka, but it turns out that Confluent Avro and Spark
Avro serialization / deserialization formats are not compatible.

"However, the Confluent Avro and Spark Avro wire formats (the result of a serializer or the source of a deserializer) are
not compatible - the Confluent Avro contains an extra magic byte (1). It is therefore not easily possible to produce Avro
messages with a Kafka producer and consume them with Spark Structured Streaming (2). It might be thus a better choice to
use the JSON format instead."
Reference: https://qimia.io/en/blog/Developing-Streaming-Applications-Spark-Structured-Streaming