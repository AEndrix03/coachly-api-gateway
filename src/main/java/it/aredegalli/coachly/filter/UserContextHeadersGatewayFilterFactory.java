package it.aredegalli.coachly.filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import it.aredegalli.coachly.user.UserIdentityClient;

@Component
public class UserContextHeadersGatewayFilterFactory
	extends AbstractGatewayFilterFactory<UserContextHeadersGatewayFilterFactory.Config> {

	private final UserIdentityClient userIdentityClient;

	public UserContextHeadersGatewayFilterFactory(UserIdentityClient userIdentityClient) {
		super(Config.class);
		this.userIdentityClient = userIdentityClient;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> exchange.getPrincipal()
			.filter(JwtAuthenticationToken.class::isInstance)
			.cast(JwtAuthenticationToken.class)
			.flatMap(authentication -> userIdentityClient.resolveUserId(authentication.getToken().getSubject())
				.map(userId -> enrichExchange(exchange, authentication, userId))
				.flatMap(chain::filter))
			.switchIfEmpty(chain.filter(exchange));
	}

	private ServerWebExchange enrichExchange(
		ServerWebExchange exchange,
		JwtAuthenticationToken authentication,
		String userId
	) {
		var jwt = authentication.getToken();
		var requestBuilder = exchange.getRequest()
			.mutate()
			.header("X-Internal-Gateway", "true")
			.header("X-User-Id", valueOrEmpty(userId))
			.header("X-Username", valueOrEmpty(jwt.getClaimAsString("preferred_username")))
			.header("X-Email", valueOrEmpty(jwt.getClaimAsString("email")))
			.header("X-Given-Name", valueOrEmpty(jwt.getClaimAsString("given_name")))
			.header("X-Family-Name", valueOrEmpty(jwt.getClaimAsString("family_name")))
			.header("X-Realm-Roles", extractRoles(jwt.getClaimAsMap("realm_access")));

		return exchange.mutate().request(requestBuilder.build()).build();
	}

	private String extractRoles(Map<String, Object> realmAccess) {
		if (realmAccess == null) {
			return "";
		}

		Object roles = realmAccess.get("roles");
		if (!(roles instanceof List<?> roleList)) {
			return "";
		}

		return roleList.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	public static class Config {
	}
}
