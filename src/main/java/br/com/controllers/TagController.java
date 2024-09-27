package br.com.controllers;

import java.util.List;

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

import br.com.dtos.TagDto;
import br.com.dtos.TaskResponse;
import br.com.services.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/tags")
@Tag(name = "Tags", description = "Endpoints for Tags management")
public class TagController {
	
	private final TagService tagService;
	
	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping(value = "/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find a Tag by id", 
	   description = "User must be authenticated and Tag must belongs to the user",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TagDto.class))), 
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<TagDto> findById(@PathVariable Long tagId){
		return ResponseEntity.ok().body(tagService.findById(tagId));
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Find all Tags by user authenticated", 
	   description = "User must be authenticated.",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", 
					   content = @Content(array = @ArraySchema(schema = @Schema(implementation = TagDto.class)))),
			   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<List<TagDto>> findAllTagsByUserAuthenticated(){
		return ResponseEntity.ok().body(tagService.findAllTagsByUserAuthenticated());
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a Tag", 
	   description = "User must be authenticated",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "Created", responseCode = "201", 
					   content = @Content(schema = @Schema(implementation = TagDto.class))),
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<TagDto> createCustomizedTag(@Valid @RequestBody TagDto tagDto){
		return ResponseEntity.created(null).body(tagService.createTag(tagDto));
	}
	
	@PatchMapping(value = "/update/{tagId}")
	@Operation(summary = "Update a Tag", 
	   description = "User must be authenticated and Tag must belongs to the user.",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", 
					   content = @Content(schema = @Schema(implementation = TagDto.class))),
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
	   }
	)
	public ResponseEntity<TagDto> updateTag(@PathVariable Long tagId,@Valid  @RequestBody TagDto tagDto){
		return ResponseEntity.ok().body(tagService.updateTag(tagId, tagDto));
	}
	
	@PatchMapping(value = "/add/{taskId}/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Add a Tag to a Task", 
	   description = "User must be authenticated. Task and Tag must belongs to the user",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TaskResponse.class))), 
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<TaskResponse> addTag(@PathVariable Long taskId, @PathVariable Long tagId){
		return ResponseEntity.ok().body(tagService.addTag(taskId, tagId));
	}
	
	@PatchMapping(value = "/remove/{taskId}/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Remove a Tag from a Task", 
	   description = "User must be authenticated. Task and Tag must belongs to the user",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "OK", responseCode = "200", content = @Content(schema = @Schema(implementation = TaskResponse.class))), 
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<TaskResponse> removeTag(@PathVariable Long taskId, @PathVariable Long tagId){
		return ResponseEntity.ok().body(tagService.removeTag(taskId, tagId));
	}
	
	@DeleteMapping(value = "/{tagId}")
	@Operation(summary = "Delete a Tag", 
	   description = "User must be authenticated and Tag must belongs to the user",
	   tags = {"Tags"},
	   responses = {
			   @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
			   @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
			   @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
			   @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)	
	   }
	)
	public ResponseEntity<Void> deleteTag(@PathVariable Long tagId){
		tagService.deleteTag(tagId);
		return ResponseEntity.noContent().build();
	}
}
