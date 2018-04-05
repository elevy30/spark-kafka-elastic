package skes.kafka.producer;

import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skes.model.User;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

import static skes.kafka.KafkaProp.KAFKA_URL;

/**
 * Created by eyallevy on 09/02/18
 */
@Service
public class SimpleUserProducerBean {

    @Value("${zookeeper.servers}")
    private String zookeeperServer;

    @Value("${consumer.output.dir}")
    private String outputFileLocation;

    @Value("${ssl.keystore.location}")
    private String sslKeystoreLocation;

    @Value("${ssl.keystore.password}")
    private String sslKeystorePassword;

    @Value("${ssl.key.password}")
    private String sslKeyPassword;

    @Value("${ssl.truststore.location}")
    private String sslTruststoreLocation;

    @Value("${ssl.truststore.password}")
    private String sslTruststorePassword;

    @Value("${kafka.security.protocol}")
    private String kafkaSecurityProtocol;

    @Value("${user.producer.name}")
    private String producerName;

    @Value("${user.topic}")
    private String topic;

    private SimpleProducer simpleProducer;

    public static void main(String[] args) throws IOException, InterruptedException {
        SimpleUserProducerBean simpleProducerBean = new SimpleUserProducerBean();
        simpleProducerBean.init();
    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        deleteTopic();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        produce();
    }

    private void produce() throws IOException, InterruptedException {
        simpleProducer = new SimpleProducer();
        User user = new User("123");
        simpleProducer.produce(prepareProducerProperties(), topic, user);
    }

    private Properties prepareProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_URL);
        props.put(ProducerConfig.ACKS_CONFIG,                   "all");
        props.put(ProducerConfig.RETRIES_CONFIG,                0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,             16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG ,             1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,          33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,   "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

//        props.put("security.protocol", kafkaSecurityProtocol);
//        if ("SSL".equals(kafkaSecurityProtocol)) {
//            props.put("ssl.keystore.location", sslKeystoreLocation);
//            props.put("ssl.keystore.password", sslKeystorePassword);
//            props.put("ssl.key.password", sslKeyPassword);
//            props.put("ssl.truststore.location", sslTruststoreLocation);
//            props.put("ssl.truststore.password", sslTruststorePassword);
//        }
        return props;
    }

    private void deleteTopic() {
        ZkClient zkClient = new ZkClient(zookeeperServer);
        ZkConnection zkConnection = new ZkConnection(zookeeperServer, 10000);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false );
        AdminUtils.deleteTopic(zkUtils, topic);
    }

    private void shutdown() {
        if (simpleProducer != null) {
            simpleProducer.close();
        }
    }
}