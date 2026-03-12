package it.aredegalli.coachly.user;

import com.github.benmanes.caffeine.cache.Cache;
import it.aredegalli.coachly.user.commons.config.CacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIdentityClientTest {

	@Test
	void resolveUserIdCachesResolvedUserId() {
		AtomicInteger invocations = new AtomicInteger();
		AtomicReference<String> requestedUrl = new AtomicReference<>();
		ExchangeFunction exchangeFunction = request -> {
			invocations.incrementAndGet();
			requestedUrl.set(request.url().toString());
			return Mono.just(ClientResponse.create(HttpStatus.OK)
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.body("{\"userId\":\"internal-user-id\"}")
				.build());
		};
		Cache<String, String> cache = CacheConfig.buildCache(Duration.ofMinutes(5), 10_000);
		UserIdentityClient client = new UserIdentityClient(
			WebClient.builder().exchangeFunction(exchangeFunction).build(),
			cache
		);

		StepVerifier.create(client.resolveUserId("jwt-subject"))
			.expectNext("internal-user-id")
			.verifyComplete();
		StepVerifier.create(client.resolveUserId("jwt-subject"))
			.expectNext("internal-user-id")
			.verifyComplete();

		assertEquals(1, invocations.get());
		assertTrue(requestedUrl.get().contains("/internal/users/resolve?externalId=jwt-subject"));
	}

	@Test
	void resolveUserIdMapsMissingIdentityToUnauthorized() {
		ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
		Cache<String, String> cache = CacheConfig.buildCache(Duration.ofMinutes(5), 10_000);
		UserIdentityClient client = new UserIdentityClient(
			WebClient.builder().exchangeFunction(exchangeFunction).build(),
			cache
		);

		StepVerifier.create(client.resolveUserId("missing-subject"))
			.expectErrorSatisfies(error -> {
				ResponseStatusException exception = (ResponseStatusException) error;
				assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
			})
			.verify();
	}
}
