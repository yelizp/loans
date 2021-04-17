 @REM call mvn clean package
set CWD=%cd%
java -Dlog4j.configuration=file:\\\%CWD%\kafka-record-gen-log4j.properties ^
    -cp %CWD%\target\kafka-record-gen-1.0-SNAPSHOT-jar-with-dependencies.jar ^
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 ^
    com.example.KafkaConsumerMain