package it.aredegalli.coachly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoachlyApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoachlyApiGatewayApplication.class, args);
	}

}
