package br.com.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.controllers.UserController;
import br.com.dtos.UserResponse;
import br.com.dtos.UserUpdateRequest;
import br.com.enums.Role;
import br.com.exceptions.BadRequestException;
import br.com.exceptions.UserNotAuthenticatedException;
import br.com.exceptions.UserNotFoundException;
import br.com.models.User;
import br.com.repositories.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PagedResourcesAssembler<UserResponse> assembler;
	
	public UserService(UserRepository userRepository, ModelMapper modelMapper, PagedResourcesAssembler<UserResponse> assembler) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
		this.assembler = assembler;
	}

	public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new UserNotAuthenticatedException("User not authenticated");
    }
	
	public PagedModel<EntityModel<UserResponse>> findAll(Pageable pageable){
		Page<User> entityPage = userRepository.findAll(pageable);
		Page<UserResponse> responsePage = entityPage.map(p -> modelMapper.map(p, UserResponse.class));
		responsePage.map(p -> p.add(linkTo(methodOn(UserController.class).findById(p.getId())).withSelfRel()));
		
		return assembler.toModel(responsePage);
	}
	
	public UserResponse findById(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
		UserResponse userResponse = modelMapper.map(user, UserResponse.class);
		userResponse.add(linkTo(methodOn(UserController.class).findById(userResponse.getId())).withSelfRel());
		return userResponse;
	}
	
	public UserResponse partialUpdate(UserUpdateRequest request) {
		User user = getAuthenticatedUser();
		
		int countChanges = 0;
		
		if(request.getUsername() != null && user.getUsername() != request.getUsername()) {
			user.setUsername(request.getUsername());
			countChanges++;
		}
		if(request.getPassword() != null && user.getPassword() != request.getPassword()) {
			String encryptPassword = new BCryptPasswordEncoder().encode(request.getPassword());
			user.setPassword(encryptPassword);
			countChanges++;
		}
		
		if(countChanges > 0) {
			UserResponse userResponse = modelMapper.map(userRepository.save(user), UserResponse.class);
			userResponse.add(linkTo(methodOn(UserController.class).findById(userResponse.getId())).withSelfRel());
			userResponse.add(linkTo(methodOn(UserController.class).partialUpdate(null)).withRel("update"));
			return userResponse;
		}
		
		throw new BadRequestException("You must input some changes");
	}
	
	public void deleteById(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
		if(user.getRole() != Role.ROLE_ADMIN) {
			userRepository.delete(user);
			return;
		}
		throw new BadRequestException("ADMIN account can not be deleted");
	}
	
	public void deleteMyAccount() {
		User user = getAuthenticatedUser();
		if(user.getRole() != Role.ROLE_ADMIN) {
			userRepository.delete(user);
			return;
		}
		throw new BadRequestException("ADMIN account can not be deleted");
	}
}
