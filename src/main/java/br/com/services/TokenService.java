package br.com.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.configs.security.JwtConfig;
import br.com.models.User;

@Service
public class TokenService {
	
	@Autowired
	JwtConfig jwtConfig;
	
	public String generateToken(User user) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(jwtConfig.jwtSecret());
			Date now = new Date();
			Date expiresAt = new Date(now.getTime() + jwtConfig.jwtExpiration());
			
			 String token = JWT.create()
					.withIssuer("auth-api")
					.withSubject(user.getUsername())
					.withExpiresAt(expiresAt)
					.sign(algorithm);
			 
			 return token;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Falha ao gerar token de acesso");
		}
	}
		
	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(jwtConfig.jwtSecret());
			return JWT.require(algorithm)
					.withIssuer("auth-api")
					.build()
					.verify(token)
					.getSubject();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Falha ao validar token de acesso");
		}
		
	}
	
}
