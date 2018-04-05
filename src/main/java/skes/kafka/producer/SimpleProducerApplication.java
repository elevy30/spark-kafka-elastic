package skes.kafka.producer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SimpleProducerApplication {

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 7778);

		new SpringApplicationBuilder()
				.sources(SimpleProducerApplication.class)
				.properties(props)
				.run(args);
	}
}
