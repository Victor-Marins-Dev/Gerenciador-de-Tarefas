package br.com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.dtos.RegisterDto;
import br.com.exceptions.BadRequestException;
import br.com.models.User;
import br.com.repositories.UserRepository;

@Service
public class AuthService implements UserDetailsService{
	
	@Autowired
	UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}
	
	public User register(RegisterDto dto) {
		if(userRepository.findByUsername(dto.getUsername()) != null) {
			throw new BadRequestException("Username already exists");
		}
		
		String encryptPassword = new BCryptPasswordEncoder().encode(dto.getPassword());
		User user = new User(dto.getUsername(), encryptPassword);
		return userRepository.save(user);
	}

}
