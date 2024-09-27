package br.com.configs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

	@Value("{$api.security.token.secret}")
	private String secret;

	@Value("${api.security.token.expiration}")
	private long expirationInMillis;

	@Bean
	public String jwtSecret() {
		return secret;
	}

	@Bean
	public long jwtExpiration() {
		return expirationInMillis;
	}
}
