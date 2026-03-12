package it.aredegalli.coachly.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class UserIdentityClientConfig {

	@Bean
	WebClient usersServiceWebClient(
		WebClient.Builder webClientBuilder,
		@Value("${coachly.services.users.url}") String usersServiceUrl
	) {
		return webClientBuilder.baseUrl(usersServiceUrl).build();
	}

	@Bean
	Cache<String, String> userIdCache(
		@Value("${coachly.services.users.cache.user-id-ttl:5m}") Duration userIdTtl,
		@Value("${coachly.services.users.cache.maximum-size:10000}") long maximumSize
	) {
		return Caffeine.newBuilder()
			.maximumSize(maximumSize)
			.expireAfterWrite(userIdTtl)
			.build();
	}
}
