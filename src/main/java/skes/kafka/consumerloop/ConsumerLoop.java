package skes.kafka.consumerloop;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static skes.kafka.KafkaProp.*;

/**
 * Created by eyallevy on 09/02/18
 */
@Slf4j
public class ConsumerLoop implements Runnable {
    private final KafkaConsumer<String, String> consumer;
    private final List<String> consumerTopics;
    private final int id;

    public static void start(String topics) {
        int numConsumers = 1;
        //noinspection UnnecessaryLocalVariable
        String groupId = GROUP;
        List<String> topicsList = Collections.singletonList(topics);

        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);

        final List<ConsumerLoop> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            ConsumerLoop consumer = new ConsumerLoop(i, groupId, topicsList);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (ConsumerLoop consumer : consumers) {
                consumer.shutdown();
            }
            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    private ConsumerLoop(int id, String groupId, List<String> topics) {
        this.id = id;
        this.consumerTopics = topics;
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,       KAFKA_URL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,                groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,      "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,      "30000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,       "latest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        this.consumer = new KafkaConsumer<>(props);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(consumerTopics);

            //noinspection InfiniteLoopStatement
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("partition", record.partition());
                    data.put("offset",    record.offset());
                    data.put("value",     record.value());
                    System.out.println(this.id + ": " + data);
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown 
        } finally {
            consumer.close();
        }
    }

    private void shutdown() {
        log.info("Shutting down consumer...");
        consumer.wakeup();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            log.info("Usage: ConsumerLoop <topicsNames> ");
            System.exit(-1);
        }

        ConsumerLoop.start(args[0]);
    }
}
