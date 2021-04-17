package com.example;

import com.example.model.AccountSchema;
import com.example.model.LoanSchema;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class KafkaAvroConsumerMain {
    private final static Logger logger = Logger.getLogger(KafkaAvroConsumerMain.class);
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "LoanConsumer_" + UUID.randomUUID());
        Consumer<Long, LoanSchema> consumer = null;
        try {
            consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList("loan_v2"));
            while (true) {
                ConsumerRecords<Long, LoanSchema> records = consumer.poll(Duration.ofMillis(100));
                if (records != null && records.count() > 0) {
                    records.forEach(record -> logger.info(record.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if(consumer != null) {
                consumer.close();
            }
        }
    }
}
