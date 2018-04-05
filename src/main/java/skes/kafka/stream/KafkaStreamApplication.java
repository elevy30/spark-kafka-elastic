package skes.kafka.stream;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class KafkaStreamApplication {

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 9999);

		new SpringApplicationBuilder()
				.sources(KafkaStreamApplication.class)
				.properties(props)
				.run(args);


//		SpringApplication.run(KafkaStreamApplication.class, args);
	}
}
