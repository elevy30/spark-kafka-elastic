package skes.kafka.consprodexample;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created on 1/17/18
 */
@Service
public class KafkaSinkBean extends KafkaBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private KafkaSink kafkaSink;

    @PostConstruct
    private void init() {
        kafkaSink = new KafkaSink(bootstrapServers, buildProperties());
        logger.info("Kafka producer configured for nodes: " + bootstrapServers);
    }

    public void send(String key, String value) {
        kafkaSink.send(key, value);
    }

    public void send(String topic, String key, String value) {
        kafkaSink.send(topic, key, value);
    }

    public void send(ProducerRecord<String, String> record) {
        kafkaSink.send(record);
    }

    public void close() {
        kafkaSink.close();
    }
}
