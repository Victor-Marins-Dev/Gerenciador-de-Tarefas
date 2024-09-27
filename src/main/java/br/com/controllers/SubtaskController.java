package br.com.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.dtos.SubtaskCreateRequest;
import br.com.dtos.SubtaskResponse;
import br.com.dtos.SubtaskUpdateRequest;
import br.com.dtos.TaskResponse;
import br.com.services.SubtaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/subtasks")
@Tag(name = "Subtasks", description = "Endpoints for subtask management")
public class SubtaskController {
	
	
	private final SubtaskService subtaskService;
	
	public SubtaskController(SubtaskService subtaskService) {
		this.subtaskService = subtaskService;
	}

	@GetMapping(value ="/{subtaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find subtask by id", 
			   description = "Only authenticated users can access this endpoint. User must be the owner of the Task",
			   tags = {"Subtasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = SubtaskResponse.class))),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<SubtaskResponse> findById(@PathVariable Long subtaskId){
		return ResponseEntity.ok().body(subtaskService.findById(subtaskId));
	}
	
	@PostMapping(value = "/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a subtask", 
			   description = "Only authenticated users can access this endpoint. Subtasks can only exist in the context of a Task",
			   tags = {"Subtasks"},
			   responses = {
					   @ApiResponse(description = "Created", responseCode = "201", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<TaskResponse> addSubtask(@PathVariable Long taskId ,@RequestBody @Valid SubtaskCreateRequest subtaskRequest){
		return ResponseEntity.ok().body(subtaskService.addSubtask(taskId, subtaskRequest));
	}
	
	@PatchMapping(value = "/{subtaskId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a subtask", 
			   description = "Only authenticated users can access this endpoint. User must be the owner of the task",
			   tags = {"Subtasks"},
			   responses = {
					   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = SubtaskResponse.class))),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<SubtaskResponse> partialUpdate(@PathVariable Long subtaskId, @Valid @RequestBody SubtaskUpdateRequest subtaskRequest){
		return ResponseEntity.ok().body(subtaskService.partialUpdate(subtaskId, subtaskRequest));	
	}
	
	@DeleteMapping("/{subtaskId}")
	@Operation(summary = "Remove a Subtask", 
			   description = "Only authenticated users can access this endpoint. User must be the owner of the task",
			   tags = {"Subtasks"},
			   responses = {
					   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
					   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content), 
					   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
					   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
			   }
	)
	public ResponseEntity<TaskResponse> removeSubtask(@PathVariable Long subtaskId){
		return ResponseEntity.ok().body(subtaskService.removeSubtask(subtaskId));
	}
}
