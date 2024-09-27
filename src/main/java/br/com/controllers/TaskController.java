package br.com.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.dtos.TaskCreateRequest;
import br.com.dtos.TaskResponse;
import br.com.dtos.TaskUpdateRequest;
import br.com.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/tasks")
@Tag(name = "Tasks", description = "Endpoints for task management")
public class TaskController {
	
	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find a task by id", 
			   description = "User must be authenticated and task must belongs the user",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TaskResponse.class))), 
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<TaskResponse> findById(@PathVariable Long taskId){ 
		return ResponseEntity.ok().body(taskService.findById(taskId));
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find all tasks by user authenticated", 
			   description = "User must be authenticated",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<PagedModel<EntityModel<TaskResponse>>> findAllByUserAuthenticated(
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "size", defaultValue = "5") Integer size
			){

		Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.ASC, "id"));
		return ResponseEntity.ok().body(taskService.findAllByUserAuthenticated(pageable));
	}
	
	@GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Perform a customized search", 
			   description = "User must be authenticated",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<PagedModel<EntityModel<TaskResponse>>> customizedSearch(
			@RequestParam(required = false) String status, 
			@RequestParam(required = false) String priority, 
			@RequestParam(required = false) String tagName,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "size", defaultValue = "5") Integer size
			){
		Pageable pageable = PageRequest.of(page, size, Sort.by(Direction.ASC, "id"));
		return ResponseEntity.ok().body(taskService.customizedSearch(status, priority, tagName, pageable));
	}
	
	@PostMapping(produces =MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a task", 
			   description = "User must be authenticated",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "Created", responseCode = "201", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest taskRequest){
		return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(taskRequest));
	}
	
	@PatchMapping(value = "/{taskId}", produces =MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a task", 
			   description = "User must be authenticated and task must belongs the user",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<TaskResponse> partialUpdate(@PathVariable Long taskId, @Valid @RequestBody TaskUpdateRequest taskRequest){
		return ResponseEntity.ok().body(taskService.partialUpdate(taskId, taskRequest));
	}
	
	@DeleteMapping("/{taskId}")
	@Operation(summary = "Delete a task", 
			   description = "User must be authenticated and task must belongs the user",
			   tags = {"Tasks"},
			   responses = {
					   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<?> delete(@PathVariable Long taskId){
		taskService.delete(taskId);
		return ResponseEntity.noContent().build();
	}
	
}
