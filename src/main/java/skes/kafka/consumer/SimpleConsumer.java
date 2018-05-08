package skes.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.codehaus.jackson.map.ObjectMapper;
import skes.model.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.StreamSupport;

/**
 * Created by eyallevy on 09/02/18
 */
@Slf4j
class SimpleConsumer implements Runnable{

    private final static ObjectMapper om = new ObjectMapper();
    private volatile boolean continuePolling = true;

    private KafkaConsumer<String, String> consumer;

    private String outputFile;
    private String topics;

    void consume(Properties props, String outputFile, String topics) throws IOException {
        List<String> topicsList = Arrays.asList((topics).split(","));
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(topicsList);

        this.outputFile = outputFile;
        this.topics = topics;

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(this);
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            while (continuePolling) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);

                StreamSupport.stream(records.spliterator(), false)
                        .map(ConsumerRecord::value)
                        .map(value -> {
                            Message message = null;
                            try {
                                message = om.readValue(value, Message.class);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //System.out.println(message.toJson() + " ---- " + value);
                            assert message != null;
                            return message.toJson() + " ---- " + value;
                        }) // trx retrieved.
                       .map(message -> message == null ? "Massage_not_parsed" : message)
                        .forEach(data -> {
                            log.info("topic: {} --  msg: {}" ,topics, data);
                            writer.println(data);
                            writer.flush();
                        });
            }
        } catch (WakeupException e) {
            // ignore for shutdown
            // logger.warn(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log.info("Closing consumer and writer");
            consumer.close();
        }
    }

    void shutdown() {
        log.info("Shutting down consumer...");
        continuePolling = false;
        consumer.wakeup();
    }


}
