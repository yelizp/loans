The project intends to solve the use case described as:

When a loan is utilized by a customer, loan and account records are created. Given these two unbounded sources (Loan and Account) implement a streaming(Spark, Flink or any other one)  application that calculates the total number of loans, total amount of loans and loans created on last minute per account type.
- Assume that maximum latency between the loan and account records is 30 seconds. Records are created at most in 30 second gap.
- Output must be calculated for each minute.
- Total amount and count must be calculated from application start. At the application start count and amount should be 0.
- Last minute count must contain the number of Loans created for the previous minute.

There are two modules:
1. customer-loans
   A Spark Structured Streaming application to consume and aggregate unbounded data sources and output the results
2. kafka-record-gen
   Produces synthetic Account and Loan records to Kafka. You may use runProducer.bat to run the module.

Requirements:
- Requires Spark 3.1.1 for Hadoop 3.2
- Requires Kafka distribution

How to run:
1. Download and unzip kafka_2.12-2.7.0. Set KAFKA_HOME environment variable to Kafka root folder
2. Start zookeeper
    %KAFKA_HOME%\bin\windows\zookeeper-server-start.bat %KAFKA_HOME%\config\zookeeper.properties
3. Start kafka
    %KAFKA_HOME%\bin\windows\kafka-server-start.bat %KAFKA_HOME%\config\server.properties
4. Start producer
    %PROJECT_ROOT%\kafka-record-gen\runProducer.bat
5. Start console consumer to debug
    %KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic loan_json --from-beginning
    %KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic account_json --from-beginning
6. Start Spark Structured Streaming application
    %PROJECT_ROOT%\consumer-loans\run.bat

Notes
- Aggregations work at micro-batch level (not allowed in continous queries)
- Sorting, distinct or chained aggregations are not supported (since data is unbounded and Spark would need to keep all the data in order to support such aggregations)
- Append and update are not supported on aggregations without watermark
- Stream to stream joins are allowed only in append mode and append mode requires watermarking

- I have put a great deal of effort to read Avro message from Confluent Kafka (see AvroConsumerLoansMain), but it turns out that Confluent Avro and Spark
Avro serialization / deserialization formats are not compatible.

"However, the Confluent Avro and Spark Avro wire formats (the result of a serializer or the source of a deserializer) are
not compatible - the Confluent Avro contains an extra magic byte (1). It is therefore not easily possible to produce Avro
messages with a Kafka producer and consume them with Spark Structured Streaming (2). It might be thus a better choice to
use the JSON format instead."
Reference: https://qimia.io/en/blog/Developing-Streaming-Applications-Spark-Structured-Streaming