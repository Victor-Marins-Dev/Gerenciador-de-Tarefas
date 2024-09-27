package br.com.dtos;

import java.io.Serializable;

public class AuthenticationDto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	
	public AuthenticationDto() {
	}

	public AuthenticationDto(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
