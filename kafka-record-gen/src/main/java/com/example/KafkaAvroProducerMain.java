package com.example;

import com.example.model.AccountSchema;
import com.example.model.LoanSchema;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class KafkaAvroProducerMain {
    private final static Logger logger = Logger.getLogger(KafkaAvroProducerMain.class);
    private final static Properties props = new Properties();
    private final static Random random = new Random();
    static {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
    }

    public static void main(String[] args) {
        produceRecords();
    }

    private static void produceRecords() {
        List<AccountSchema> accounts = new ArrayList<>();
        accounts.add(new AccountSchema(1L,1));
        accounts.add(new AccountSchema(4L, 1));
        accounts.add(new AccountSchema(3L,2));
        accounts.add(new AccountSchema(7L,2));
        accounts.add(new AccountSchema(8L,3));

        List<LoanSchema> loans = new ArrayList<>();
        loans.add(new LoanSchema(1L,1L, 10000.00));
        loans.add(new LoanSchema(2L,3L,2000.00));
        loans.add(new LoanSchema(3L,4L,3000.00));
        loans.add(new LoanSchema(4L,7L,6000.00));
        loans.add(new LoanSchema(5L,8L,6000.00));

        AccountSchema account = null;
        LoanSchema loan = null;

        while(true) {
            for(int i=0; i < loans.size(); i++) {
                account = accounts.get(i);
                produceAccountRecord(account);
                sleep();
                loan = loans.get(i);
                produceLoanRecord(loan);
                sleep();
            }
        }
    }

    private static void sleep() {
        int msToSleep = (random.nextInt(3) + 1) * 10000;
        try {
            Thread.sleep(msToSleep);
        } catch (Exception e) {}
    }

    private static void produceAccountRecord(AccountSchema account) {
        Producer<Long, AccountSchema> producer = null;
        try {
            producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
            producer.send(new ProducerRecord<Long, AccountSchema>("account_v2", account.getAccountId(), account));
            logger.info("Produced account record: " + account.toString());
        } catch (Exception e) {
            logger.error("Error producing account record: " + account.toString());
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if(producer != null) {
                producer.flush();
                producer.close();
            }
        }
    }

    private static void produceLoanRecord(LoanSchema loan) {
        Producer<Long, LoanSchema> producer = null;
        try {
            producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
            producer.send(new ProducerRecord<Long, LoanSchema>("loan_v2", loan.getLoanId(), loan));
            logger.info("Produced loan record: " + loan.toString());
        } catch (Exception e) {
            logger.error("Error producing loan record: " + loan.toString());
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if(producer != null) {
                producer.flush();
                producer.close();
            }
        }
    }
}

