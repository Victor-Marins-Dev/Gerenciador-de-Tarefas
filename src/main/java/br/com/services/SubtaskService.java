package br.com.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import br.com.controllers.SubtaskController;
import br.com.dtos.SubtaskCreateRequest;
import br.com.dtos.SubtaskResponse;
import br.com.dtos.SubtaskUpdateRequest;
import br.com.dtos.TaskResponse;
import br.com.enums.TaskStatus;
import br.com.exceptions.BadRequestException;
import br.com.models.Subtask;
import br.com.models.Task;
import br.com.repositories.SubtaskRepository;

@Service
public class SubtaskService {
	
	private final SubtaskRepository subtaskRepository;
	private final TaskService taskService;
	private final ModelMapper modelMapper;
	
	public SubtaskService(SubtaskRepository subtaskRepository, TaskService taskService, ModelMapper modelMapper) {
		this.subtaskRepository = subtaskRepository;
		this.taskService = taskService;
		this.modelMapper = modelMapper;
	}

	public SubtaskResponse findById(Long id) {
		Subtask subtask = subtaskRepository.findById(id).orElseThrow(() -> new BadRequestException("Subtask not found"));
		Task task = subtask.getTask();
		
		taskService.checkingTaskOwnership(task);
		
		SubtaskResponse subtaskResponse = modelMapper.map(subtask, SubtaskResponse.class);	
		return addLinks(subtaskResponse);
	}
	
	public TaskResponse addSubtask(Long taskId, SubtaskCreateRequest subtaskRequest) {
		Task task = taskService.findTaskOrThrow(taskId);
		
		taskService.checkingTaskOwnership(task);
		
		Subtask subtask = modelMapper.map(subtaskRequest, Subtask.class);
		subtask = preFirstPersist(subtask);
		
		subtask.setTask(task);
		subtaskRepository.save(subtask);
		return taskService.addLinksToATask(modelMapper.map(task, TaskResponse.class));
	}
	
	public TaskResponse removeSubtask(Long subtaskId) {
		Subtask subtask = subtaskRepository.findById(subtaskId).orElseThrow(() -> new BadRequestException("Subtask not found"));
		Task task = subtask.getTask();
		
		taskService.checkingTaskOwnership(task);
		
		subtaskRepository.deleteById(subtaskId);;
		
		return taskService.addLinksToATask(modelMapper.map(task, TaskResponse.class));
	}
	
	public SubtaskResponse partialUpdate(Long subtaskId, SubtaskUpdateRequest request) {
		Subtask subtask = subtaskRepository.findById(subtaskId).orElseThrow(() -> new BadRequestException("Subtask not found"));	
		Task task = subtask.getTask();
		
		taskService.checkingTaskOwnership(task);
		
		int countChanges = 0;
		
		if(request.getTitle() != null) {
			subtask.setTitle(request.getTitle());
			countChanges++;
		}
		if(request.getDescription() != null) {
			subtask.setDescription(request.getDescription());
			countChanges++;
		}
		if(request.getDueDate() != null) {
			subtask.setDueDate(request.getDueDate());
			countChanges++;
		}
		if(request.getStatus() != null) {
			subtask.setStatus(request.getStatus());
			countChanges++;
		}
		if(countChanges > 0) {
			SubtaskResponse subtaskResponse = modelMapper.map(subtaskRepository.save(subtask), SubtaskResponse.class);
			return addLinks(subtaskResponse);
		}
		throw new BadRequestException("Please provide updates");
	}
	
	private SubtaskResponse addLinks(SubtaskResponse subtaskResponse) {
		subtaskResponse.add(linkTo(methodOn(SubtaskController.class).findById(subtaskResponse.getId())).withSelfRel());
		subtaskResponse.add(linkTo(methodOn(SubtaskController.class).removeSubtask(subtaskResponse.getId())).withRel("removeSubtask"));
		subtaskResponse.add(linkTo(methodOn(SubtaskController.class).partialUpdate(subtaskResponse.getId(), null)).withRel("update"));
		
		return subtaskResponse;
	}
	
	private Subtask preFirstPersist(Subtask subtask) {

		if(subtask.getCreatedDate() == null) {
			subtask.setCreatedDate(LocalDate.now());
		}
		if(subtask.getStatus() == null) {
			subtask.setStatus(TaskStatus.UNDONE); 
		}
		return subtask;
	}
	
}
