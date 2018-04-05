package skes.kafka.stream;

import lombok.Data;

import java.util.Set;

/**
 * Created by eyallevy on 22/08/2017
 */
@Data
class ConsumerConfigurationDto {
    private String bootstrapServers;
    private String topics;
    private Float minScore;
    private Set<Long> analysisIds;
    private Integer triggeredFeaturesSize;
    private String consumerName;
}
