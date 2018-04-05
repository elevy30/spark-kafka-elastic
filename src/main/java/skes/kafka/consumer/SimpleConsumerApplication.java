package skes.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootApplication
@ComponentScan ("skes.kafka.consumer")
public class SimpleConsumerApplication {

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 7777);

		new SpringApplicationBuilder()
				.sources(SimpleConsumerApplication.class)
				.properties(props)
				.run(args);
	}
}
