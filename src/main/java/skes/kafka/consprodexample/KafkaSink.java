package skes.kafka.consprodexample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by eyallevy on 17/08/2017
 */
public class KafkaSink implements Serializable{

    private Producer<String,String> producer;
    private String defaultTopic;

    public KafkaSink(String bootstrapServers) {
        this(bootstrapServers, new Properties());
    }

    public KafkaSink(String bootstrapServers, Properties extraProperties) {
        //TODO externalize configurations if needed
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.putAll(extraProperties);
        producer = new KafkaProducer<>(props);
    }

    public KafkaSink(String bootstrapServers, String defaultTopic) {
        this(bootstrapServers);
        this.defaultTopic = defaultTopic;
    }

    public void send(String key, String value) {
        producer.send(new ProducerRecord<>(defaultTopic, key, value));
    }

    public void send(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value));
    }

    public void send(ProducerRecord<String, String> record) {
        producer.send(record);
    }

    public void close() {
        this.producer.close();
    }
}
