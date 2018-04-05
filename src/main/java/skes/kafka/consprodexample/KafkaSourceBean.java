package skes.kafka.consprodexample;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * Created on 1/17/18
 */
@Service
public class KafkaSourceBean extends KafkaBean {

    @Value("${group.id}")
    private String groupId;

    private KafkaSource kafkaSource;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void init() {
        kafkaSource = new KafkaSource(bootstrapServers, groupId, buildProperties());
        logger.info("Kafka consumer configured for nodes: " + bootstrapServers);
    }

    public KafkaSourceBean subscribe(Collection<String> topics) {
        kafkaSource.subscribe(topics);
        return this;
    }

    public ConsumerRecords<String, String> poll(long timeout) {
        return kafkaSource.poll(timeout);
    }

    public KafkaSourceBean unsubscribe() {
        kafkaSource.unsubscribe();
        return this;
    }

    public void close() {
        kafkaSource.close();
    }

    public void shutdown() {
        kafkaSource.shutdown();
    }
}
