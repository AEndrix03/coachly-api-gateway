package it.aredegalli.coachly.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Value("${coachly.security.cors.allowed-origins:*}")
	private String allowedOrigins;

	@Value("${coachly.security.cors.allowed-methods:*}")
	private String allowedMethods;

	@Value("${coachly.security.cors.allowed-headers:*}")
	private String allowedHeaders;

	@Value("${coachly.security.cors.allow-credentials:false}")
	private boolean allowCredentials;

	@Value("${coachly.security.cors.max-age:3600}")
	private long maxAge;

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(toList(allowedOrigins));
		configuration.setAllowedMethods(toList(allowedMethods));
		configuration.setAllowedHeaders(toList(allowedHeaders));
		configuration.setAllowCredentials(allowCredentials);
		configuration.setMaxAge(maxAge);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private List<String> toList(String rawValue) {
		return List.of(rawValue.split(","))
			.stream()
			.map(String::trim)
			.filter(value -> !value.isEmpty())
			.toList();
	}
}
