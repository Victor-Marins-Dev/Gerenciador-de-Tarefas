package br.com.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.dtos.UserResponse;
import br.com.dtos.UserUpdateRequest;
import br.com.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/users")
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {
	
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Finds all users", 
			   description = "Only admins can access this endpoint",
			   tags = {"Users"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))), 
					   @ApiResponse(description = "No Content", responseCode = "204", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<PagedModel<EntityModel<UserResponse>>> findAll(
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "size", defaultValue = "5") Integer size
			){
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok().body(userService.findAll(pageable));
	}
	
	
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find user by id",
			   description = "Only admins can access this endpoint",
			   tags = {"Users"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class))), 
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }		   
	)
	public ResponseEntity<UserResponse> findById(@PathVariable Long id){
		return ResponseEntity.ok().body(userService.findById(id));
	}
	
	@PatchMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update username and/or password",
	   		   description = "An authenticated user can update your login credentials",
	   		   tags = {"Users"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class))), 
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	  	  
	   }		   
)
	public ResponseEntity<UserResponse> partialUpdate(@Valid @RequestBody UserUpdateRequest dto){
		return ResponseEntity.ok().body(userService.partialUpdate(dto));
	}
	
	@DeleteMapping("/{userId}")
	@Operation(summary = "Delete user by id",
	   description = "Only admins can access this endpoint. Admins account can not be deleted",
	   tags = {"Users"},
	   responses = {
			   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),  
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	  	  
	   }		   
)
	public ResponseEntity<Void> deleteById(@PathVariable Long userId){
		userService.deleteById(userId);
		return ResponseEntity.noContent().build();
	}
	
	@DeleteMapping("/delete/account")
	@Operation(summary = "Delete user account",
	   description = "User can delete your own account. Admins account can not be deleted",
	   tags = {"Users"},
	   responses = {
			   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),  
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	  	  
	   }		   
)
	public ResponseEntity<Void> deleteMyAccount(){
		userService.deleteMyAccount();
		return ResponseEntity.noContent().build();
	}
}
