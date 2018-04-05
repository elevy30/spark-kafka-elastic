package skes.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;


/**
 * Created by eyallevy on 16/02/18 .
 */
@Service
public class ConsumerProps {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServer;

    @Value("${kafka.security.protocol}")
    private String kafkaSecurityProtocol;

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

    Properties props;

    @PostConstruct
    public void prepareConsumerProperties() throws IOException {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,       bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,                "1");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,      "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,      "30000");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,       "latest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

//        props.put("security.protocol", kafkaSecurityProtocol);
//        if ("SSL".equals(kafkaSecurityProtocol)) {
//            props.put("ssl.keystore.location", sslKeystoreLocation);
//            props.put("ssl.keystore.password", sslKeystorePassword);
//            props.put("ssl.key.password", sslKeyPassword);
//            props.put("ssl.truststore.location", sslTruststoreLocation);
//            props.put("ssl.truststore.password", sslTruststorePassword);
//        }

        this.props = props;
    }
}
