package br.com.configs.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.exceptions.UserNotAuthenticatedException;
import br.com.repositories.UserRepository;
import br.com.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

	
	private final TokenService tokenService;

	private final UserRepository userRepository;
	
	public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
		this.tokenService = tokenService;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String requestURI = request.getRequestURI();
	    if (requestURI.startsWith("/api/auth/")  || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/swagger-ui/") || requestURI.startsWith("/swagger-ui.html")) {
	    	filterChain.doFilter(request, response);
	        return;
	    }

		var token = this.recoverToken(request);
		if (token == null) {
			throw new UserNotAuthenticatedException("User not authenticated");
		}
		
		var username = tokenService.validateToken(token);
		UserDetails user = userRepository.findByUsername(username);

		var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		filterChain.doFilter(request, response);

	}

	private String recoverToken(HttpServletRequest request) {
		var authHeader = request.getHeader("Authorization");
		if (authHeader == null) {
			return null;
		}
		return authHeader.replace("Bearer ", "");
	}

}
