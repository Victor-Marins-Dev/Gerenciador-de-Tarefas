package br.com.services ; 

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.controllers.SubtaskController;
import br.com.controllers.TagController;
import br.com.controllers.TaskController;
import br.com.dtos.TaskCreateRequest;
import br.com.dtos.TaskResponse;
import br.com.dtos.TaskUpdateRequest;
import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import br.com.exceptions.BadRequestException;
import br.com.models.Task;
import br.com.models.User;
import br.com.repositories.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;
	private final UserService userService;
	private final ModelMapper modelMapper;
	private final PagedResourcesAssembler<TaskResponse> assembler;

	public TaskService(TaskRepository taskRepository, UserService userService, ModelMapper modelMapper, PagedResourcesAssembler<TaskResponse> assembler) {
		this.taskRepository = taskRepository;
		this.userService = userService;
		this.modelMapper = modelMapper;
		this.assembler = assembler;
	}

	public TaskResponse findById(Long taskId) {
		Task task = taskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Task not found"));
		checkingTaskOwnership(task);
		TaskResponse taskResponse = modelMapper.map(task, TaskResponse.class);
		return addLinksToATask(taskResponse);
	}
	
	public PagedModel<EntityModel<TaskResponse>> findAllByUserAuthenticated(Pageable pageable){
		User user = userService.getAuthenticatedUser();
		Page<Task> entityPage = taskRepository.findAllByUserId(user.getId(), pageable);
		Page<TaskResponse> responsePage = entityPage.map(p -> modelMapper.map(p, TaskResponse.class));
		responsePage = responsePage.map(p -> p.add(linkTo(methodOn(TaskController.class).findById(p.getId())).withSelfRel()));
		return assembler.toModel(responsePage);
	}
	
	public PagedModel<EntityModel<TaskResponse>> customizedSearch(String status, String priority, String tagName, Pageable pageable){
		User user = userService.getAuthenticatedUser();
		
		if(status != null) {
			status = status.toUpperCase();
		}
		if(priority != null) {
			priority = priority.toUpperCase();
		}
		if(tagName != null) {
			tagName = tagName.toUpperCase();
		}
		
		Page<Task> entityPage = taskRepository.customizedSearch(user.getId(), status, priority, tagName, pageable);
		Page<TaskResponse> responsePage = entityPage.map(p -> modelMapper.map(p, TaskResponse.class));
		responsePage.map(p -> p.add(linkTo(methodOn(TaskController.class).findById(p.getId())).withSelfRel()));
		return assembler.toModel(responsePage);
	}
	
	public TaskResponse create(TaskCreateRequest taskRequest) {
		Task task = modelMapper.map(taskRequest, Task.class);
		task = prePersistTask(task);
		TaskResponse taskResponse = modelMapper.map(taskRepository.save(task), TaskResponse.class);
		return addLinksToATask(taskResponse);
	}
	
	public TaskResponse partialUpdate(Long taskId, TaskUpdateRequest taskRequest) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new BadRequestException("Task not found"));
		
		checkingTaskOwnership(task);
		
		applyUpdatesToATask(task, taskRequest);
			
		TaskResponse taskResponse = modelMapper.map(taskRepository.save(task), TaskResponse.class);
		return addLinksToATask(taskResponse);
		
	}
	
	public void delete(Long taskId) {
		Task task = taskRepository.findById(taskId).orElseThrow(() -> new BadRequestException("Task not found"));
		checkingTaskOwnership(task);
		taskRepository.deleteById(taskId);
		return;
	}
	
	public void checkingTaskOwnership(Task task) {
		User userAuthenticated = userService.getAuthenticatedUser();
		User userFromTask = task.getUser();
		if(!userAuthenticated.equals(userFromTask)) throw new AccessDeniedException("Task doesn't belong to the user");
	}
	
	TaskResponse addLinksToATask(TaskResponse taskDto) {
		taskDto.add(linkTo(methodOn(TaskController.class).findById(taskDto.getId())).withSelfRel());
		taskDto.add(linkTo(methodOn(TaskController.class).partialUpdate(taskDto.getId(), null)).withRel("update"));
		taskDto.add(linkTo(methodOn(TaskController.class).delete(taskDto.getId())).withRel("delete"));
		taskDto.add(linkTo(methodOn(TaskController.class).findAllByUserAuthenticated(null, null)).withRel("findAllTasks"));
		taskDto.add(linkTo(methodOn(SubtaskController.class).addSubtask(taskDto.getId(), null)).withRel("addSubtask"));
		taskDto.add(linkTo(methodOn(TagController.class).addTag(taskDto.getId(), null)).withRel("addTag"));	
		return taskDto;
	}
	
	public Task findTaskOrThrow(Long taskId) {
		return taskRepository.findById(taskId)
				.orElseThrow(() -> new BadRequestException("Task not found"));
	}
	
	Task applyUpdatesToATask(Task task, TaskUpdateRequest taskRequest) {
		
		int countChanges = 0;
		
    	if(taskRequest.getTitle() != null && !taskRequest.getTitle().isBlank()) {
			task.setTitle(taskRequest.getTitle());
			countChanges++;
		}
		if(taskRequest.getDescription() != null) {
			task.setDescription(taskRequest.getDescription());
			countChanges++;
		}
		if(taskRequest.getStatus() != null) {
			task.setStatus(taskRequest.getStatus());
			countChanges++;
		}
		if(taskRequest.getPriority() != null) {
			task.setPriority(taskRequest.getPriority());
			countChanges++;
		}
		if(taskRequest.getDueDate() != null) {
			task.setDueDate(taskRequest.getDueDate());
			countChanges++;
		}
		
		if(countChanges == 0) throw new BadRequestException("Please provide updates");
		
		return task;
	}
	
	Task prePersistTask(Task task) {
		User user = userService.getAuthenticatedUser();
		task.setUser(user);
		
		if(task.getCreatedDate() == null) {
			task.setCreatedDate(LocalDate.now());
		}
		if(task.getStatus() == null) {
			task.setStatus(TaskStatus.UNDONE); 
		}
		if(task.getPriority() == null) {
			task.setPriority(TaskPriority.NONE); 
		}
		return task;
	}
	
	
}

