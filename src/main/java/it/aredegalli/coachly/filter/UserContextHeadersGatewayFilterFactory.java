package it.aredegalli.coachly.filter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.aredegalli.coachly.user.commons.utils.constants.AuditConstants;
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
			.header(AuditConstants.INTERNAL_GATEWAY_HEADER, "true")
			.header(AuditConstants.USER_ID_HEADER, valueOrEmpty(userId))
			.header(AuditConstants.USERNAME_HEADER, valueOrEmpty(jwt.getClaimAsString("preferred_username")))
			.header(AuditConstants.EMAIL_HEADER, valueOrEmpty(jwt.getClaimAsString("email")))
			.header(AuditConstants.GIVEN_NAME_HEADER, valueOrEmpty(jwt.getClaimAsString("given_name")))
			.header(AuditConstants.FAMILY_NAME_HEADER, valueOrEmpty(jwt.getClaimAsString("family_name")))
			.header(AuditConstants.REALM_ROLES_HEADER, extractRoles(jwt.getClaimAsMap("realm_access")));

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
