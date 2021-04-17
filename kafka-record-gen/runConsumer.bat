@REM call mvn clean package
set CWD=%cd%
java -Dlog4j.configuration=file:\\\%CWD%\kafka-record-gen-log4j.properties ^
    -cp %CWD%\target\kafka-record-gen-1.0-SNAPSHOT-jar-with-dependencies.jar ^
    com.example.KafkaConsumerMain