package br.com.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.dtos.AuthenticationDto;
import br.com.dtos.RegisterDto;
import br.com.dtos.TokenDto;
import br.com.models.User;
import br.com.services.AuthService;
import br.com.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/auth")
@Tag(name = "Auth", description = "Endpoints for register and login")
public class AuthController {
	
	@Autowired
	AuthService authService;
	
	@Autowired
	TokenService tokenService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Login", 
	   description = "Endpoint for login",
	   tags = {"Auth"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = String.class))),
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<?> login(@RequestBody @Valid AuthenticationDto dto) {
		var usernamePassword = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
		var auth = this.authenticationManager.authenticate(usernamePassword);
		
		var token= tokenService.generateToken((User)auth.getPrincipal());
		
		return ResponseEntity.ok().body(new TokenDto(token));
		   	
	}
	
	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Register", 
	   description = "Endpoint for register",
	   tags = {"Auth"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", content = @Content),
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<Void> register(@RequestBody @Valid RegisterDto dto){
		authService.register(dto);
		return ResponseEntity.created(null).build();
	}		
}
