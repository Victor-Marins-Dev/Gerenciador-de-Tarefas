package br.com.dtos;

import java.io.Serializable;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Size(min = 3, max = 30, message = "The username size must be between 3 and 30 characters")
	private String username;
	
	@Pattern(
		    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,20}$",
		    message = "The password must contain at least one digit, one uppercase letter, one lowercase letter, one special character and be between 8 and 20 characters long"
		)
	private String password;
	
	public UserUpdateRequest() {
	}

	public UserUpdateRequest(
			@Size(min = 3, max = 30, message = "The username size must be between 3 and 30 characters") String username,
			@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,20}$", message = "The password must contain at least one digit, one uppercase letter, one lowercase letter, one special character and be between 8 and 20 characters long") String password) {
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
