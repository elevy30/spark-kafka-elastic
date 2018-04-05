package skes.kafka.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skes.kafka.stream.zookeeper.ZookeeperService;

import javax.annotation.PostConstruct;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@Slf4j
@Service
public class ConsumerRegistrationService {

    @Value("${zookeeper.consumer.config.path}")
    private String configurationBasePath;

    private ZookeeperService zookeeperService;

    @Autowired
    public ConsumerRegistrationService (ZookeeperService zookeeperService){
        this.zookeeperService  = zookeeperService;
    }

    private final Serializer<ConsumerConfigurationDto> configurationDtoSerializer = new JsonPOJOSerializer<>();
    private final Deserializer<ConsumerConfigurationDto> configurationDtoDeserializer = new JsonPOJODeserializer<>();

    private Map<String, Object> serdeProps = new HashMap<>();

    @PostConstruct
    public void init() {
        serdeProps = new HashMap<>();
        serdeProps.put("JsonPOJOClass", ConsumerConfigurationDto.class);
        configurationDtoSerializer.configure(serdeProps, false);
        configurationDtoDeserializer.configure(serdeProps, false);
    }

    public ConsumerConfigurationDto register(Float minScore, Set<Long> analysisIds, Integer triggeredFeaturesSize, boolean reset, String consumerName, String outputTopic) throws KeeperException, InterruptedException {
        log.info("register consumer: " + consumerName + " to zookeeper.");
        ConsumerConfigurationDto consumerConfigurationDto = new ConsumerConfigurationDto();
        initConfig(minScore, analysisIds, triggeredFeaturesSize, consumerName, outputTopic, consumerConfigurationDto);

        byte[] data = configurationDtoSerializer.serialize(null, consumerConfigurationDto);
        zookeeperService.create(String.join("/", configurationBasePath, consumerConfigurationDto.getConsumerName()), data);

        return consumerConfigurationDto;
    }

    void unregister(String consumerName) throws KeeperException, InterruptedException {
        log.info("unregister consumer: " + consumerName + " from zookeeper.");
        zookeeperService.delete(String.join("/", configurationBasePath, consumerName));
    }

    public boolean isConsumerRegistered(String consumerName) {
        return false;
    }

    Set<ConsumerConfigurationDto> getConsumerConfigurations() throws KeeperException, InterruptedException {
        /*Set<ConsumerConfigurationDto> consumerConfigurationDtos = zookeeperService.getZnodeChildren(configurationBasePath).stream().map(
                s -> {
                    try {
                        return zookeeperService.getZNodeData(String.join("/", configurationBasePath, s), false);
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).collect(Collectors.toList());*/

        Set<ConsumerConfigurationDto> consumerConfigurationDtos = new HashSet<>();
        List<String> zNodeChildren = zookeeperService.getZNodeChildren(configurationBasePath);
        zNodeChildren.forEach(child -> {
            try {
                String data = (String) zookeeperService.getZNodeData(String.join("/", configurationBasePath, child), false);
                consumerConfigurationDtos.add(configurationDtoDeserializer.deserialize(null, data.getBytes()));
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        return consumerConfigurationDtos;
    }

    private void initConfig(Float minScore, Set<Long> analysisIds, Integer triggeredFeaturesSize, String consumerName, String outputTopic, ConsumerConfigurationDto consumerConfigurationDto) {
        consumerConfigurationDto.setTopics(outputTopic);
        consumerConfigurationDto.setMinScore(minScore);
        consumerConfigurationDto.setAnalysisIds(analysisIds);
        consumerConfigurationDto.setTriggeredFeaturesSize(triggeredFeaturesSize);
        consumerConfigurationDto.setConsumerName(consumerName);
    }
}
