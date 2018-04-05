package skes.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by eyallevy on 09/02/18
 */
@SuppressWarnings("Duplicates")
@Slf4j
@Component
public class SimpleTrxConsumerBean {

    @Value("${trx.consumer.name}")
    private String consumerName;

    @Value("${trx.topic}")
    private String topics;

    @Value("${consumer.output.dir}")
    private String outputFileLocation;


    private ConsumerProps consumerProps;
    private SimpleConsumer simpleConsumer;

    @Autowired
    public SimpleTrxConsumerBean(ConsumerProps consumerProps) {
        this.consumerProps = consumerProps;
    }

    @PostConstruct
    public void init() throws IOException {
        log.info("Start SimpleTrxConsumerBean");
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        consume();
    }

    private void consume() throws IOException {
        simpleConsumer = new SimpleConsumer();
        File dir = new File(outputFileLocation);
        Properties props = new Properties();
        props.putAll(consumerProps.props);
        simpleConsumer.consume(props, new File(dir, consumerName + ".log").getAbsolutePath(), topics);
    }

    private void shutdown() {
        if (simpleConsumer != null) {
            simpleConsumer.shutdown();
        }
    }

}