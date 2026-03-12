package it.aredegalli.coachly.filter;

import it.aredegalli.coachly.user.UserIdentityClient;
import it.aredegalli.coachly.user.commons.utils.constants.AuditConstants;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserContextHeadersGatewayFilterFactoryTest {

	@Test
	void applyAddsResolvedUserIdHeader() {
		UserIdentityClient userIdentityClient = mock(UserIdentityClient.class);
		when(userIdentityClient.resolveUserId("jwt-subject")).thenReturn(Mono.just("internal-user-id"));

		UserContextHeadersGatewayFilterFactory factory = new UserContextHeadersGatewayFilterFactory(userIdentityClient);
		JwtAuthenticationToken authentication = new JwtAuthenticationToken(Jwt.withTokenValue("token")
			.header("alg", "none")
			.subject("jwt-subject")
			.claim("preferred_username", "coachly-user")
			.claim("email", "user@example.com")
			.claim("realm_access", Map.of("roles", List.of("athlete")))
			.build());
		ServerWebExchange exchange = authenticatedExchange(authentication);
		AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
		GatewayFilterChain chain = forwarded -> {
			forwardedExchange.set(forwarded);
			return Mono.empty();
		};

		StepVerifier.create(factory.apply(new UserContextHeadersGatewayFilterFactory.Config()).filter(exchange, chain))
			.verifyComplete();

		HttpHeaders headers = forwardedExchange.get().getRequest().getHeaders();
		assertEquals("internal-user-id", headers.getFirst(AuditConstants.USER_ID_HEADER));
		assertEquals("coachly-user", headers.getFirst(AuditConstants.USERNAME_HEADER));
		assertEquals("user@example.com", headers.getFirst(AuditConstants.EMAIL_HEADER));
		assertEquals("athlete", headers.getFirst(AuditConstants.REALM_ROLES_HEADER));
	}

	@Test
	void applySkipsLookupWhenPrincipalIsMissing() {
		UserIdentityClient userIdentityClient = mock(UserIdentityClient.class);
		UserContextHeadersGatewayFilterFactory factory = new UserContextHeadersGatewayFilterFactory(userIdentityClient);
		ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/workouts/user").build());
		GatewayFilterChain chain = forwarded -> Mono.empty();

		StepVerifier.create(factory.apply(new UserContextHeadersGatewayFilterFactory.Config()).filter(exchange, chain))
			.verifyComplete();

		verifyNoInteractions(userIdentityClient);
	}

	@SuppressWarnings("unchecked")
	private ServerWebExchange authenticatedExchange(JwtAuthenticationToken authentication) {
		ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/workouts/user").build());
		return new ServerWebExchangeDecorator(exchange) {
			@Override
			public <T extends Principal> Mono<T> getPrincipal() {
				return Mono.just((T) authentication);
			}
		};
	}
}
