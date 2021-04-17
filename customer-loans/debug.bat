set SPARK_KAFKA_VERSION=0.10
set SPARK_SUBMIT_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

call mvn clean package
spark-submit2 ^
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.1.1,org.apache.kafka:kafka-clients:2.7.0 ^
    --master local ^
    --class com.example.JsonCustomerLoansMain ^
    .\target\customer-loans-1.0-SNAPSHOT-jar-with-dependencies.jar