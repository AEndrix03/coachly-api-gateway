package it.aredegalli.coachly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class UserIdentityClientConfig {

	@Bean
	WebClient.Builder webClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	WebClient usersServiceWebClient(
		WebClient.Builder webClientBuilder,
		@Value("${coachly.services.users.url}") String usersServiceUrl
	) {
		return webClientBuilder.baseUrl(usersServiceUrl).build();
	}
}
