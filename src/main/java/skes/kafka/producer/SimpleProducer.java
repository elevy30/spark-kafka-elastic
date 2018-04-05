package skes.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import skes.model.Message;
import skes.model.User;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by eyallevy on 09/02/18
 */
@SuppressWarnings("FieldCanBeLocal")
@Slf4j
class SimpleProducer {

    private KafkaProducer<String, String> producer;
    private volatile boolean continueSending = true;

    void produce(Properties props, String topic ,Message message) throws IOException, InterruptedException {
        producer = prepareProducer(props);

            while (continueSending) {
                log.info("send new msg info to {}",topic );
                send(topic, "KAFKA_KEY::" + message.key(), message.toJson());
                Thread.sleep(600000);
            }
    }

    private KafkaProducer<String, String> prepareProducer(Properties props) {
        return new KafkaProducer<>(props);
    }

    private void send(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value));
    }

    public void close() {
        this.producer.close();
    }
}
