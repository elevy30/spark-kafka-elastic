package skes.spark.streaming;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SparkStreamingApplication {

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 8888);

		new SpringApplicationBuilder()
				.sources(SparkStreamingApplication.class)
				.properties(props)
				.run(args);
	}
}
