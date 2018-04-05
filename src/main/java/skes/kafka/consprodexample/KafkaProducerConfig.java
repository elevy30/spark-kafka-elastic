package skes.kafka.consprodexample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

/**
 * Created by anatolytikhonov on 20/08/2017.
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@FieldDefaults(makeFinal = true)
public class KafkaProducerConfig implements Serializable {
    String bootstrapServers;
    String topic;
}
