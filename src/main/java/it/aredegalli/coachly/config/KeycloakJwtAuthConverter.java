package it.aredegalli.coachly.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class KeycloakJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = new ArrayList<>(defaultAuthoritiesConverter.convert(jwt));
		authorities.addAll(extractRealmRoles(jwt));

		String principalName = jwt.getClaimAsString("preferred_username");
		if (principalName == null || principalName.isBlank()) {
			principalName = jwt.getSubject();
		}

		return new JwtAuthenticationToken(jwt, authorities, principalName);
	}

	private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess == null) {
			return Set.of();
		}

		Object rolesClaim = realmAccess.get("roles");
		if (!(rolesClaim instanceof Collection<?> roles)) {
			return Set.of();
		}

		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		for (Object role : roles) {
			String roleName = String.valueOf(role).trim();
			if (!roleName.isEmpty()) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
			}
		}

		return authorities;
	}
}
