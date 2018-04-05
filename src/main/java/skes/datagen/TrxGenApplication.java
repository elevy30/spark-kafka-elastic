package skes.datagen;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class TrxGenApplication {

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 5555);

		new SpringApplicationBuilder()
				.sources(TrxGenApplication.class)
				.properties(props)
				.run(args);
	}
}
