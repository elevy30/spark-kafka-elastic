package skes.kafka.consprodexample;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collection;
import java.util.Properties;

/**
 * Created by anatolytikhonov on 26/12/2017.
 */
public class KafkaSource {

    private KafkaConsumer<String, String> consumer;

    public KafkaSource(String bootstrapServers, String groupId) {
        this(bootstrapServers, groupId, new Properties());
    }

    public KafkaSource(String bootstrapServers, String groupId, Properties extraProperties) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset","latest");
        props.putAll(extraProperties);
        this.consumer = new KafkaConsumer<>(props);
    }

    public KafkaSource subscribe(Collection<String> topics) {
        consumer.subscribe(topics);
        return this;
    }

    public ConsumerRecords<String, String> poll(long timeout) {
        return consumer.poll(timeout);
    }

    public KafkaSource unsubscribe() {
        consumer.unsubscribe();
        return this;
    }

    public void close() {
        consumer.close();
    }

    public void shutdown() {
        consumer.wakeup();
    }
}
