package it.aredegalli.coachly.user;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class UserIdentityClient {

	private final WebClient usersServiceWebClient;
	private final Cache<String, String> userIdCache;

	public UserIdentityClient(WebClient usersServiceWebClient, Cache<String, String> userIdCache) {
		this.usersServiceWebClient = usersServiceWebClient;
		this.userIdCache = userIdCache;
	}

	public Mono<String> resolveUserId(String externalId) {
		if (externalId == null || externalId.isBlank()) {
			return Mono.error(new ResponseStatusException(
				HttpStatus.UNAUTHORIZED,
				"Authenticated token is missing the subject claim"
			));
		}

		String cacheKey = externalId;
		String cachedUserId = userIdCache.getIfPresent(cacheKey);
		if (cachedUserId != null) {
			return Mono.just(cachedUserId);
		}

		return usersServiceWebClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/internal/users/resolve")
				.queryParam("externalId", externalId)
				.build())
			.retrieve()
			.onStatus(
				status -> status.value() == HttpStatus.NOT_FOUND.value(),
				response -> Mono.error(new ResponseStatusException(
					HttpStatus.UNAUTHORIZED,
					"No internal user mapped to the authenticated subject"
				))
			)
			.onStatus(
				status -> status.is5xxServerError(),
				response -> Mono.error(new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Unable to resolve the authenticated user"
				))
			)
			.onStatus(
				status -> status.is4xxClientError(),
				response -> Mono.error(new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Identity resolution failed because user-be rejected the request"
				))
			)
			.bodyToMono(UserIdentityResolveResponse.class)
			.map(UserIdentityResolveResponse::userId)
			.doOnNext(userId -> userIdCache.put(cacheKey, userId))
			.onErrorMap(
				WebClientRequestException.class,
				ex -> new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Unable to reach user-be for identity resolution",
					ex
				)
			);
	}
}
