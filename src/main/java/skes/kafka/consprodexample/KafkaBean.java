package skes.kafka.consprodexample;

import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

/**
 * Created on 1/17/18
 */
public abstract class KafkaBean {

    @Value("${ssl.keystore.location}")
    protected String sslKeystoreLocation;

    @Value("${ssl.keystore.password}")
    protected String sslKeystorePassword;

    @Value("${ssl.key.password}")
    protected String sslKeyPassword;

    @Value("${ssl.truststore.location}")
    protected String sslTruststoreLocation;

    @Value("${ssl.truststore.password}")
    protected String sslTruststorePassword;

    @Value("${kafka.security.protocol}")
    protected String kafkaSecurityProtocol;

    @Value("${kafka.bootstrap.servers}")
    protected String bootstrapServers;

    Properties buildProperties() {
        Properties props = new Properties();
        props.put("security.protocol", kafkaSecurityProtocol);
        if ("SSL".equals(kafkaSecurityProtocol)) {
            props.put("ssl.keystore.location", sslKeystoreLocation);
            props.put("ssl.keystore.password", sslKeystorePassword);
            props.put("ssl.key.password", sslKeyPassword);
            props.put("ssl.truststore.location", sslTruststoreLocation);
            props.put("ssl.truststore.password", sslTruststorePassword);
        }
        return props;
    }
}
