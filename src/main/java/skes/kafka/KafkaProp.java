package skes.kafka;

/**
 * Created by eyallevy on 09/02/18
 */
public class KafkaProp {

    private static final String KAFKA_HOST  = "kafka-local";
    private static final String KAFKA_PORT  = "9092";
    public static final  String KAFKA_URL   = KAFKA_HOST + ":" + KAFKA_PORT;

    public final static String GROUP = "consumer-group";
}