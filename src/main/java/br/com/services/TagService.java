package br.com.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.controllers.TagController;
import br.com.dtos.TagDto;
import br.com.dtos.TaskResponse;
import br.com.exceptions.BadRequestException;
import br.com.models.Tag;
import br.com.models.Task;
import br.com.models.User;
import br.com.repositories.TagRepository;

@Service
public class TagService {
	
	private final TagRepository tagRepository;
	private final TaskService taskService;
	private final UserService userService;
	private final ModelMapper modelMapper;
	
	public TagService(TagRepository taskTagRepository, TaskService taskService, UserService userService, ModelMapper modelMapper) {
		this.tagRepository = taskTagRepository;
		this.taskService = taskService;
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	public TagDto findById(Long id) {
		Tag tag = tagRepository.findById(id).orElseThrow(() -> new BadRequestException("Tag not found!"));
		
		checkingTagOwnership(tag);
		
		TagDto tagDto = modelMapper.map(tag, TagDto.class);
		
		tagDto = addLinksToTags(tagDto);
		return tagDto;
	}
	
	public List<TagDto> findAllTagsByUserAuthenticated() {
		User user = userService.getAuthenticatedUser();
		List<Tag> list = tagRepository.findAllByUserId(user.getId());
		List<TagDto> listTagDto = new ArrayList<>();
		for (Tag tag : list) {
			TagDto tagDto = modelMapper.map(tag, TagDto.class);
			tagDto.add(linkTo(methodOn(TagController.class).findById(tagDto.getId())).withSelfRel());
			listTagDto.add(tagDto);
		}
		return listTagDto;
	}
	
	public TagDto createTag(TagDto tagDto) {
		User user = userService.getAuthenticatedUser();
		int numberOfTags = user.getTags().size();
		
		if(numberOfTags >= 10) throw new BadRequestException("User can not have more than 10 Customized Tags");
		
		Tag tag = new Tag(tagDto.getName(), user);
		tag = tagRepository.save(prePersist(tag));
		tagDto = modelMapper.map(tag, TagDto.class);
		return addLinksToTags(tagDto);
	}
	
	public TagDto updateTag(Long tagId, TagDto tagDto) {
		Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new BadRequestException("Tag not found"));
		
		checkingTagOwnership(tag);
		
		tag.setName(tagDto.getName());
		
		tagRepository.save(prePersist(tag));
		tagDto = modelMapper.map(tag, TagDto.class);
		tagDto = addLinksToTags(tagDto);
		return tagDto;
	}
	
	public TaskResponse addTag(Long taskId, Long tagId) {
		Task task = taskService.findTaskOrThrow(taskId);
		
		taskService.checkingTaskOwnership(task);
		
		Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new BadRequestException("Tag not found"));
		
		checkingTagOwnership(tag);
		
		task.addTag(tag);
		tag.addTask(task);
		tagRepository.save(tag);
		return taskService.addLinksToATask(modelMapper.map(task, TaskResponse.class));
	}
	
	public TaskResponse removeTag(Long taskId, Long tagId) {
		Task task = taskService.findTaskOrThrow(taskId);
		
		taskService.checkingTaskOwnership(task);
		
		Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new BadRequestException("Tag not found"));
		
		checkingTagOwnership(tag);	
		
		tag.removeTask(task);
		tagRepository.save(tag);
		return taskService.addLinksToATask(modelMapper.map(task, TaskResponse.class));
	}
	
	public void deleteTag(Long tagId) {
		Tag tag = tagRepository.findById(tagId).orElseThrow(() -> new BadRequestException("Tag not found"));
		
		checkingTagOwnership(tag);
		
	    if (!tag.getTasks().isEmpty()) {
	        for (Task task : tag.getTasks()) {
	            task.getTags().remove(tag); 
	        }
	    }
		
		tagRepository.delete(tag);
		return;	
	}
	
	protected void checkingTagOwnership(Tag tag) {
		User userFromTag = tag.getUser();
		User userAuthenticated = userService.getAuthenticatedUser();
		if(!userFromTag.equals(userAuthenticated)) throw new AccessDeniedException("Tag doesn't belong to the user");
	}
	
	private TagDto addLinksToTags(TagDto tagDto) {
		tagDto.add(linkTo(methodOn(TagController.class).findById(tagDto.getId())).withSelfRel());
		tagDto.add(linkTo(methodOn(TagController.class).findAllTagsByUserAuthenticated()).withRel("findAllTags"));
		tagDto.add(linkTo(methodOn(TagController.class).updateTag(tagDto.getId(), null)).withRel("update"));
		tagDto.add(linkTo(methodOn(TagController.class).deleteTag(tagDto.getId())).withRel("delete"));
		return tagDto;
	}
	
	public Tag prePersist(Tag tag) {
		tag.setName(tag.getName().toUpperCase());
		return tag;
	}
	
}
