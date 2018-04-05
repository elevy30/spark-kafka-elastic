package skes.kafka.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.zookeeper.KeeperException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;
import skes.model.AppName;
import skes.model.User;
import skes.model.UserTrx;
import skes.model.Trx;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import static skes.kafka.stream.MessageLogger.*;


/**
 * Created by eyallevy on 8/13/17
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "SameParameterValue"})
@Service
@Slf4j
public class KsTopologyBuilder {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String KEY_DELIMITER = "::";
    private static final String JOIN_ONLINE_APP_NAME = "joinOnlineApp";


    private Serde<User> userSerde;
    private Serde<Trx> trxSerde;
    private Serde<UserTrx> userTrxSerde;

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.advertised.servers}")
    private String advertisedKafkaServers;

    @Value("${detect.to.classify.max.delay.ms:1000000}")
    private Long joinGap;

    @Value("${streams.app.state.store.path}")
    private String stateStorePath;

    @Value("${messages.log.level:DEBUG}")
    private LogLevel level;

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

    @Value("${user.topic}")
    private String userTopic;

    @Value("${trx.topic}")
    private String trxTopic;

    @Value("${joined.topic}")
    private String joinedTopic;

    private ConcurrentMap<String, KafkaStreams> appNameToStreamsInstance = new ConcurrentHashMap<>();

    private ConsumerRegistrationService consumerRegistrationService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public KsTopologyBuilder(ConsumerRegistrationService consumerRegistrationService) {
        this.consumerRegistrationService = consumerRegistrationService;
    }

    @PostConstruct
    public void init() throws KeeperException, InterruptedException {
        MessageLogger.logger = logger;
        prepareSerializers();
        //deploy basic topologies
        KafkaStreams kafkaStreams = deployJoinTopology(true);
    }

    private void prepareSerializers() {
        Map<String, Object> serdeProps = new HashMap<>();

        final Serializer<Trx> trxSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", Trx.class);
        trxSerializer.configure(serdeProps, false);
        final Deserializer<Trx> trxDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", Trx.class);
        trxDeserializer.configure(serdeProps, false);
        trxSerde = Serdes.serdeFrom(trxSerializer, trxDeserializer);

        final Serializer<User> userSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", User.class);
        userSerializer.configure(serdeProps, false);
        final Deserializer<User> userDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", User.class);
        userDeserializer.configure(serdeProps, false);
        userSerde = Serdes.serdeFrom(userSerializer, userDeserializer);

        final Serializer<UserTrx> joinedSerializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", UserTrx.class);
        joinedSerializer.configure(serdeProps, false);
        final Deserializer<UserTrx> joinedDeserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", UserTrx.class);
        joinedDeserializer.configure(serdeProps, false);
        userTrxSerde = Serdes.serdeFrom(joinedSerializer, joinedDeserializer);
    }

    private KafkaStreams deployJoinTopology(boolean reset) throws KeeperException, InterruptedException {
        logger.info("### Starting user trx join topology ... ###");
        Properties streamsConfiguration = initStreamConfig(AppName.join_user_trx);
        StreamsBuilder builder = new StreamsBuilder();

        KTable<String, User> inputUserTable = builder.table(userTopic, Consumed.with(Serdes.String(), userSerde));

        KStream<String, Trx> inputTrxStream = builder.stream(trxTopic, Consumed.with(Serdes.String(), trxSerde));

        /*
        Create joined stream <user, trx> from stream trx and lookup table of users.
        The key of stream is set to be EYAL::<userid json>
        */
        KStream<String, UserTrx> userTrxKStream = inputTrxStream
                .leftJoin(
                        inputUserTable,
                        UserTrx::new,
                        Joined.with(Serdes.String(), trxSerde, userSerde)
                )
                .selectKey( (k, v) -> "UserTrx::" + v.getTrx().getTrxId()) //indexName::docID
                .peek((key, joinedObject) -> log(level, "Rich trx from TRX-stream.. key: {} ; value: {}", key, joinedObject));

        // Convert output to json and send to topic output
        userTrxKStream.to(joinedTopic, Produced.with(Serdes.String(), userTrxSerde));

        KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
        smartDeploy("myKafkaStreaming", streams, reset);
        logger.info("Join Online app instance is running.");
        return streams;
    }

    private Properties initStreamConfig(AppName appName) {
        return initStreamConfig(appName, null);
    }

    private Properties initStreamConfig(AppName appName, String postfix) {
        Properties streamsConfiguration = new Properties();
        String applicationId = postfix == null ? appName.name() : String.join("-", appName.name(), postfix);
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5 * 1000);
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateStorePath);

        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//        streamsConfiguration.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaSecurityProtocol);
//        streamsConfiguration.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslKeystoreLocation);
//        streamsConfiguration.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslKeystorePassword);
//        streamsConfiguration.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslKeyPassword);
//        streamsConfiguration.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslTruststoreLocation);
//        streamsConfiguration.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslTruststorePassword);
        return streamsConfiguration;
    }

    public void unregisterConsumerAndShutdownProcessingTopology(String consumerName) {
        String appName = buildAppName(AppName.join_user_trx.name(), consumerName);
        if (appNameToStreamsInstance.get(appName) != null) {
            appNameToStreamsInstance.get(appName).close();
            appNameToStreamsInstance.remove(appName);
        }
        try {
            consumerRegistrationService.unregister(consumerName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("could not delete consumer config from zookeeper");
        }
    }

    public Set<ConsumerConfigurationDto> getAllConsumerConfigurations() throws KeeperException, InterruptedException {
        return consumerRegistrationService.getConsumerConfigurations();
    }

    public void unDeployOnlineJoinTopology() {
        logger.info("Closing online join topology ...");
        KafkaStreams streams = appNameToStreamsInstance.get(JOIN_ONLINE_APP_NAME);
        if (streams != null) {
            streams.close();
            streams.cleanUp();
            //TODO add synchronous close
        }
        appNameToStreamsInstance.remove(JOIN_ONLINE_APP_NAME);
        logger.info("Online join topology successfully closed");
    }

    private void smartDeploy(String appName, KafkaStreams newApp, Boolean reset) throws KeeperException, InterruptedException {
        smartDeploy(appName, newApp, reset, null);
    }

    private void smartDeploy(String appName, KafkaStreams newApp, Boolean reset, String registrationName) throws KeeperException, InterruptedException {
        if (appNameToStreamsInstance.containsKey(appName)) {
            logger.info("App already running, redeploy ...");
            KafkaStreams oldApp = appNameToStreamsInstance.get(appName);
            oldApp.close();
            if (reset) {
                oldApp.cleanUp();
                logger.info("State store is cleaned up. All KTables are empty now");
            }
            if (registrationName != null) {
                consumerRegistrationService.unregister(registrationName);
            }
        }
        appNameToStreamsInstance.put(appName, newApp);
        streamsSyncStart(newApp);
    }

    @PreDestroy
    public void closeKafkaStreams() {
        logger.info("Shutting down all stream instances");
        appNameToStreamsInstance.forEach((k, v) -> v.close());
    }

    private String buildAppName(String prefix, String name) {
        return prefix + "-" + name;
    }

    private void streamsSyncStart(KafkaStreams streams) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (newState.equals(KafkaStreams.State.RUNNING)) {
                countDownLatch.countDown();
            }
        });
        streams.start();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
