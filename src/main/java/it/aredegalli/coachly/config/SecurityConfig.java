package it.aredegalli.coachly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(exchanges -> exchanges
				.pathMatchers("/actuator/health", "/actuator/info").permitAll()
				.pathMatchers(HttpMethod.OPTIONS).permitAll()
				.pathMatchers("/public/**").permitAll()
				.pathMatchers("/api/**").authenticated()
				.anyExchange().denyAll())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
				new ReactiveJwtAuthenticationConverterAdapter(new KeycloakJwtAuthConverter()))))
			.build();
	}
}
