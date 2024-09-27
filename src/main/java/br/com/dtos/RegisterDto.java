package br.com.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterDto {
	
	@NotBlank
	@Size(min = 3, max = 30, message = "The username size must be between 3 and 30 characters")
	private String username;
	@Pattern(
		    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,20}$",
		    message = "The password must contain at least one digit, one uppercase letter, one lowercase letter, one special character and be between 8 and 20 characters long"
		)
	private String password;
	
	public RegisterDto() {
	}

	public RegisterDto(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(@NotBlank @Size(min = 3, max = 30) String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(@Pattern(
		    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,20}$",
		    message = "The password must contain at least one digit, one uppercase letter, one lowercase letter, one special character and be between 8 and 20 characters long") String password) {
		this.password = password;
	}	
}
