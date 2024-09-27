package br.com.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.security.access.AccessDeniedException;

import br.com.controllers.TaskController;
import br.com.dtos.TaskCreateRequest;
import br.com.dtos.TaskResponse;
import br.com.dtos.TaskUpdateRequest;
import br.com.enums.Role;
import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import br.com.exceptions.BadRequestException;
import br.com.exceptions.UserNotAuthenticatedException;
import br.com.models.Subtask;
import br.com.models.Tag;
import br.com.models.Task;
import br.com.models.User;
import br.com.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
	
	@Mock
	private TaskRepository taskRepository;
	
	@Mock
	private UserService userService;
	
	@Mock
	private ModelMapper modelMapper;
	
	@Mock
	private PagedResourcesAssembler<TaskResponse> assembler;
	
	@Captor
	ArgumentCaptor<Long> longCaptor;
	
	@Captor
	ArgumentCaptor<Page<TaskResponse>> pageCaptor;
	
	@Captor
	ArgumentCaptor<Task> taskCaptor;
	
	@Captor
	ArgumentCaptor<String> stringCaptor;
	
	private Pageable pageable = PageRequest.of(0, 5);
	
	@InjectMocks
	private TaskService taskService;
	
	private void mockModelMapperMap() {
	    doAnswer(invocation -> {
	        Task taskArgument = invocation.getArgument(0);
	        TaskResponse response = new TaskResponse();
	        response.setId(taskArgument.getId());
	        response.setTitle(taskArgument.getTitle());
	        response.setDescription(taskArgument.getDescription());
	        response.setStatus(taskArgument.getStatus());
	        response.setPriority(taskArgument.getPriority());
	        response.setCreatedDate(taskArgument.getCreatedDate());
	        response.setDueDate(taskArgument.getDueDate());
	        response.setSubtasks(taskArgument.getSubtasks());
	        response.setTags(taskArgument.getTags());
	        return response;
	    }).when(modelMapper).map(any(Task.class), eq(TaskResponse.class));
	}
	
	@Nested
	class FindById {
		@Test
		void findById_ShouldReturnTaskResponse(){
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Subtask subtask = new Subtask("Subtask 1");
			
			Tag tag = new Tag();
			tag.setName("Tag 1");
			
			Task task = new Task.Builder()
		                .id(1L)
		                .user(user)
		                .title("Task 1")
		                .description("Description 1")
		                .status(TaskStatus.UNDONE)
		                .priority(TaskPriority.LOW)
		                .createdDate(LocalDate.now())
		                .dueDate(LocalDate.of(2024, 10, 18))
		                .build();
			
			task.addSubtask(subtask);
			task.addTag(tag);
			
			mockModelMapperMap();
			
			doReturn(Optional.of(task)).when(taskRepository).findById(1L);
			doReturn(user).when(userService).getAuthenticatedUser();

			TaskResponse taskResponse = taskService.findById(1L);
				
			assertNotNull(taskResponse);
			assertThat(taskResponse.getId()).isEqualTo(task.getId());
			assertThat(taskResponse.getDescription()).isEqualTo(task.getDescription());
			assertThat(taskResponse.getStatus()).isEqualTo(task.getStatus());
			assertThat(taskResponse.getPriority()).isEqualTo(task.getPriority());
			assertThat(taskResponse.getCreatedDate()).isEqualTo(task.getCreatedDate());
			assertThat(taskResponse.getDueDate()).isEqualTo(task.getDueDate());
			assertThat(taskResponse.getSubtasks()).isEqualTo(task.getSubtasks());
			assertThat(taskResponse.getTags()).isEqualTo(task.getTags());	
		}
			
		@Test
		void findById_ShouldThrowAccessNotFoundExceptionWhenUserTryToAccessTaskThatIsNotYours(){
			
			User user1 = new User(1L, "Joao", "password", Role.ROLE_USER);
			User user2 = new User(2L, "Mario", "password", Role.ROLE_USER);
			
			Task taskFromUser2 = new Task.Builder()
		                .id(1L)
		                .user(user2)
		                .title("Task 1")
		                .build();
			
			doReturn(Optional.of(taskFromUser2)).when(taskRepository).findById(taskFromUser2.getId());
			doReturn(user1).when(userService).getAuthenticatedUser();

			AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> taskService.findById(taskFromUser2.getId()));
			assertThat(ex.getMessage()).isEqualTo("Task doesn't belong to the user");
		}	
	}
	
	@Nested
	class FindAllByUserAuthenticated {
		@Test
		void findAllByUserAuthenticated_ShouldReturnPagedModelWithCorrectTaskTitlesAndLinksForAuthenticatedUser(){
			
			User user1 = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task1 = new Task.Builder()
	                .id(1L)
	                .user(user1)
	                .title("Task 1")
	                .build();
			
			Task task2 = new Task.Builder()
	                .id(2L)
	                .user(user1)
	                .title("Task 2")
	                .build();
			
			Page<Task> taskPage = new PageImpl<>(List.of(task1, task2));
			
			when(userService.getAuthenticatedUser()).thenReturn(user1);
			when(taskRepository.findAllByUserId(user1.getId(), pageable)).thenReturn(taskPage);
			
			mockModelMapperMap();
			
	        TaskResponse taskResponse1 = new TaskResponse();
	        taskResponse1.setId(1L);
	        taskResponse1.setTitle("Task 1");
	      
	        TaskResponse taskResponse2 = new TaskResponse();
	        taskResponse2.setId(2L);
	        taskResponse2.setDescription("Task 2");
	        
	        EntityModel<TaskResponse> entityModel1 = EntityModel.of(taskResponse1)
	        		.add(linkTo(methodOn(TaskController.class).findById(1L)).withSelfRel());
	        EntityModel<TaskResponse> entityModel2 = EntityModel.of(taskResponse2)
	        		.add(linkTo(methodOn(TaskController.class).findById(2L)).withSelfRel());

	        PagedModel<EntityModel<TaskResponse>> mockPagedModel = PagedModel.of(
	                List.of(entityModel1, entityModel2),
	                new PageMetadata(2, 0, 2, 1)
	        );

	        when(assembler.toModel(pageCaptor.capture())).thenReturn(mockPagedModel);
			
			PagedModel<EntityModel<TaskResponse>> result = taskService.findAllByUserAuthenticated(pageable);
			
			verify(userService).getAuthenticatedUser();
			verify(taskRepository).findAllByUserId(user1.getId(), pageable);
			verify(modelMapper, times(2)).map(any(Task.class), eq(TaskResponse.class));
			verify(assembler).toModel(pageCaptor.getValue());	
			
			assertNotNull(result);
			assertThat(result.getContent().size()).isEqualTo(2);
			
			for (EntityModel<TaskResponse> entityModel : result) {
				
				assertTrue(entityModel.getLinks().stream()
			            .anyMatch(link -> "self".equals(link.getRel().value())));
				
				TaskResponse response = entityModel.getContent();
				
				if(response.getId() == 1L) assertThat(response.getTitle() == taskResponse1.getTitle());
				if(response.getId() == 2L) assertThat(response.getTitle() == taskResponse2.getTitle());	
			}
		}

		@Test
		void findAllByUserAuthenticated_ShouldThrowUserNotAuthenticatedExceptionWhenUserNotAuthenticated(){
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
				
			when(userService.getAuthenticatedUser()).thenThrow(new UserNotAuthenticatedException("User not authenticated"));
				
			assertThrows(UserNotAuthenticatedException.class, () -> taskService.findAllByUserAuthenticated(pageable));
			
			verify(userService).getAuthenticatedUser();
			verify(taskRepository, never()).findAllByUserId(user.getId(), pageable);
		}
	}
	
	@Nested
	class CustomizedSearch {
		@Test
		void customizedSearch_ShouldReturnCorrectlyPagedModel() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task1 = new Task.Builder()
	                .id(1L)
	                .user(user)
	                .title("Task 1")
	                .build();
			
			Task task2 = new Task.Builder()
	                .id(2L)
	                .user(user)
	                .title("Task 2")
	                .build();
			
			String status = null;
			String priority = null;
			String tagName = null;
			
			Page<Task> entityPage = new PageImpl<>(List.of(task1, task2));
			
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.customizedSearch(user.getId(), status, priority, tagName, pageable)).thenReturn(entityPage);
			
			mockModelMapperMap();
			
			TaskResponse taskResponse1 = new TaskResponse();
	        taskResponse1.setId(1L);
	        taskResponse1.setTitle("Task 1");
	      
	        TaskResponse taskResponse2 = new TaskResponse();
	        taskResponse2.setId(2L);
	        taskResponse2.setDescription("Task 2");
	        
	        EntityModel<TaskResponse> entityModel1 = EntityModel.of(taskResponse1)
	        		.add(linkTo(methodOn(TaskController.class).findById(1L)).withSelfRel());
	        EntityModel<TaskResponse> entityModel2 = EntityModel.of(taskResponse2)
	        		.add(linkTo(methodOn(TaskController.class).findById(2L)).withSelfRel());

	        PagedModel<EntityModel<TaskResponse>> mockPagedModel = PagedModel.of(
	                List.of(entityModel1, entityModel2),
	                new PageMetadata(2, 0, 2, 1)
	        );
	        
	        when(assembler.toModel(pageCaptor.capture())).thenReturn(mockPagedModel);
	        
	        PagedModel<EntityModel<TaskResponse>> result = taskService.customizedSearch(status, priority, tagName, pageable);
			
	        verify(userService).getAuthenticatedUser();
			verify(taskRepository).customizedSearch(user.getId(), status, priority, tagName, pageable);
			verify(modelMapper, times((int) entityPage.getTotalElements())).map(any(Task.class), eq(TaskResponse.class));
			verify(assembler).toModel(pageCaptor.getValue());	
			
	        assertNotNull(result);
			assertThat(result.getContent().size()).isEqualTo(entityPage.getTotalElements());
			
			for (EntityModel<TaskResponse> entityModel : result) {
				
				assertTrue(entityModel.getLinks().stream()
			            .anyMatch(link -> "self".equals(link.getRel().value())));
				
				TaskResponse response = entityModel.getContent();
				
				if(response.getId() == 1L) assertThat(response.getTitle() == taskResponse1.getTitle());
				if(response.getId() == 2L) assertThat(response.getTitle() == taskResponse2.getTitle());	
			}
		}
		
		@Test
		void customizedSearch_ShouldTransformParamsToUpperCaseWhenNotNull() {
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task1 = new Task.Builder()
	                .id(1L)
	                .user(user)
	                .title("Task 1")
	                .build();
			
			Task task2 = new Task.Builder()
	                .id(2L)
	                .user(user)
	                .title("Task 2")
	                .build();
				
			Page<Task> entityPage = new PageImpl<>(List.of(task1, task2));
				
			String status = "done";
			String priority = "none";
			String tagName = "Study";	
	
			ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> priorityCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> tagNameCaptor = ArgumentCaptor.forClass(String.class);
			
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.customizedSearch(
					eq(user.getId()), 
					statusCaptor.capture(), 
					priorityCaptor.capture(), 
					tagNameCaptor.capture(), 
					eq(pageable))).thenReturn(entityPage);
			
			mockModelMapperMap();
			
			taskService.customizedSearch(status, priority, tagName, pageable);
			
			assertThat(statusCaptor.getValue()).isEqualTo(status.toUpperCase());
			assertThat(priorityCaptor.getValue()).isEqualTo(priority.toUpperCase());
			assertThat(tagNameCaptor.getValue()).isEqualTo(tagName.toUpperCase());
			
			verify(taskRepository).customizedSearch(user.getId(), statusCaptor.getValue(), priorityCaptor.getValue(), tagNameCaptor.getValue(), pageable);	
		}
		
		@Test
		public void customizedSearch_ShouldThrowUserNotAuthenticatedExceptionWhenUserNotAuthenticated() {
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			when(userService.getAuthenticatedUser()).thenThrow(new UserNotAuthenticatedException("User not authenticated"));
			
			assertThrows(UserNotAuthenticatedException.class, () -> taskService.customizedSearch(null, null, null, pageable));
			
			verify(taskRepository, never()).customizedSearch(user.getId(), null, null, null, pageable);
		}
	}
	
	@Nested
	class Create {
		
		@Test
		void create_ShouldCreateTaskAndReturnExpectedResponseWithLinks() {
			
			TaskService spyTaskService = Mockito.spy(taskService);
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", null, null, null, null);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();

			when(modelMapper.map(taskRequest, Task.class)).thenReturn(task);
			
			Task taskUpdated = new Task.Builder()
		            .id(1L)
		            .title("Task 1")
		            .user(user)
		            .createdDate(LocalDate.of(1998, 3, 12))
		            .status(TaskStatus.UNDONE)
		            .priority(TaskPriority.NONE)
		            .build();
			
			doReturn(taskUpdated).when(spyTaskService).prePersistTask(task);
				
			when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
			mockModelMapperMap();
			
			TaskResponse result = spyTaskService.create(taskRequest);
			
			verify(spyTaskService).prePersistTask(taskCaptor.capture());
		    Task capturedTaskFromPrePersist = taskCaptor.getValue();

		    assertThat(capturedTaskFromPrePersist.getId()).isEqualTo(task.getId());
		    assertThat(capturedTaskFromPrePersist.getTitle()).isEqualTo(task.getTitle());

		    verify(taskRepository).save(taskCaptor.capture());
		    Task capturedTaskFromSave = taskCaptor.getValue();

		    assertThat(capturedTaskFromSave.getUser()).isEqualTo(user);
		    assertThat(capturedTaskFromSave.getCreatedDate()).isEqualTo(taskUpdated.getCreatedDate());
			
			assertThat(result.getId()).isEqualTo(taskUpdated.getId());
			assertThat(result.getTitle()).isEqualTo(taskUpdated.getTitle());
			assertThat(result.getDescription()).isEqualTo(taskUpdated.getDescription());
			assertThat(result.getDueDate()).isEqualTo(taskUpdated.getDueDate());
			assertThat(result.getStatus()).isEqualTo(taskUpdated.getStatus());
			assertThat(result.getPriority()).isEqualTo(taskUpdated.getPriority());
			
			assertThat(result.getLinks())
	        .extracting(link -> link.getRel().value(), link -> link.getHref())
	        .containsExactlyInAnyOrder(
	            Tuple.tuple("self", "/api/tasks/1"),
	            Tuple.tuple("update", "/api/tasks/1"),
	            Tuple.tuple("delete", "/api/tasks/1"),
	            Tuple.tuple("findAllTasks", "/api/tasks{?page,size}"),
	            Tuple.tuple("addSubtask", "/api/subtasks/1"),
	            Tuple.tuple("addTag", "/api/tags/add/1/{tagId}")
	        	); 

		    verify(modelMapper).map(taskRequest, Task.class);
		    verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void create_ShouldThrowUserNotAuthenticatedExceptionWhenUserNotAuthenticated() {
			TaskCreateRequest taskRequest = new TaskCreateRequest("Task 1", null, null, null, null);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();

			when(modelMapper.map(taskRequest, Task.class)).thenReturn(task);
			when(taskService.prePersistTask(task)).thenThrow(new UserNotAuthenticatedException("User not authenticated"));
			
			UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () -> taskService.create(taskRequest));
			assertThat(ex.getMessage()).isEqualTo("User not authenticated");
			verify(modelMapper).map(taskRequest, Task.class);
			verify(taskRepository, never()).save(task);
		}
	}
	
	@Nested
	class PartialUpdate {
		
		@Test
		void partialUpdate_ShouldUpdateOnlyTitleWhenOnlyTitleIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setTitle("Task 1 updated");
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(updates.getTitle());
			assertThat(result.getDescription()).isNull();
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateOnlyDescriptionWhenOnlyDescriptionIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setDescription("Description 1 updated");
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			assertThat(result.getDescription()).isEqualTo(updates.getDescription());
			assertThat(result.getStatus()).isNull();
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateOnlyStatusWhenOnlyStatusIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setStatus(TaskStatus.DONE);
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			assertThat(result.getDescription()).isNull();
			assertThat(result.getStatus()).isEqualTo(updates.getStatus());
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateOnlyPriorityWhenOnlyPriorityIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setPriority(TaskPriority.LOW);
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			assertThat(result.getDescription()).isNull();
			assertThat(result.getPriority()).isEqualTo(updates.getPriority());
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateOnlyDueDateWhenOnlyDueDateIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setDueDate(LocalDate.of(3050, 12, 03));
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			assertThat(result.getDescription()).isNull();
			assertThat(result.getDueDate()).isEqualTo(updates.getDueDate());
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateTitleAndDescriptionWhenGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setTitle("Task 1 updated");
			updates.setDescription("Description 1 updated");
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(updates.getTitle());
			assertThat(result.getDescription()).isEqualTo(updates.getDescription());
			assertThat(result.getDueDate()).isNull();
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldUpdateAllUpdatesParamsWhenGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setTitle("Task 1 updated");
			updates.setDescription("Description 1 updated");
			updates.setStatus(TaskStatus.UNDONE);
			updates.setPriority(TaskPriority.HIGH);
			updates.setDueDate(LocalDate.of(3000, 12, 03));
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(updates.getTitle());
			assertThat(result.getDescription()).isEqualTo(updates.getDescription());
			assertThat(result.getDueDate()).isEqualTo(updates.getDueDate());
			assertThat(result.getStatus()).isEqualTo(updates.getStatus());
			assertThat(result.getPriority()).isEqualTo(updates.getPriority());
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldAddLinks() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			updates.setTitle("Task 1 updated");
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			when(taskRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			
			mockModelMapperMap();
			
			TaskResponse result = taskService.partialUpdate(task.getId(), updates);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(updates.getTitle());
			assertThat(result.getDescription()).isNull();
			
			assertThat(result.getLinks())
	        .extracting(link -> link.getRel().value(), link -> link.getHref())
	        .containsExactlyInAnyOrder(
	            Tuple.tuple("self", "/api/tasks/1"),
	            Tuple.tuple("update", "/api/tasks/1"),
	            Tuple.tuple("delete", "/api/tasks/1"),
	            Tuple.tuple("findAllTasks", "/api/tasks{?page,size}"),
	            Tuple.tuple("addSubtask", "/api/subtasks/1"),
	            Tuple.tuple("addTag", "/api/tags/add/1/{tagId}")
	        	); 
			
			verify(taskRepository).findById(task.getId());
			verify(taskRepository).save(any(Task.class));
			verify(modelMapper).map(any(Task.class), eq(TaskResponse.class));
		}
		
		@Test
		void partialUpdate_ShouldThrowBadRequestExceptionWhenTaskNotFound() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenThrow(new UserNotAuthenticatedException("User not authenticated"));
			
			UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, 
					() -> taskService.partialUpdate(task.getId(), updates));
			
			assertThat(ex.getMessage()).isEqualTo("User not authenticated");
			verify(taskRepository).findById(task.getId());
			verify(taskRepository, never()).save(any(Task.class));
			
		}
		
		@Test
		void partialUpdate_ShouldThrowUserNotAuthenticatedExceptionWhenUserNotAuthenticated() {
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			
			when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());
			
			BadRequestException ex = assertThrows(BadRequestException.class, 
					() -> taskService.partialUpdate(anyLong(), updates));
			
			assertThat(ex.getMessage()).isEqualTo("Task not found");
			verify(taskRepository).findById(anyLong());
			verify(taskRepository, never()).save(any(Task.class));
			
		}
		
		@Test
		void partialUpdate_ShouldThrowBadRequestExceptionWhenNothingIsGiven() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			TaskUpdateRequest updates = new TaskUpdateRequest();
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			when(userService.getAuthenticatedUser()).thenReturn(user);
			
			BadRequestException ex = assertThrows(BadRequestException.class, 
					() -> taskService.partialUpdate(task.getId(), updates));
			
			assertThat(ex.getMessage()).isEqualTo("Please provide updates");
			verify(taskRepository).findById(task.getId());
			verify(taskRepository, never()).save(any(Task.class));
			
		}
	}
	
	@Nested
	class Delete {
		
	}
	
	@Nested
	class CheckingTaskOwnership {
		
		@Test
		void checkingTaskOwnership_ShouldNotThrowExceptionWhenTaskBelongsToTheUser() {
			
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user);
			
			when(userService.getAuthenticatedUser()).thenReturn(user);
		
			assertDoesNotThrow(() -> taskService.checkingTaskOwnership(task));
			assertThat(task.getUser()).isEqualTo(user);
		}
		
		@Test
		void checkingTaskOwnership_ShouldThrowExceptionWhenTaskDoesNotBelongToTheUser() {
			
			User user1 = new User(1L, "Joao", "password", Role.ROLE_USER);
			User user2 = new User(2L, "Mario", "password", Role.ROLE_USER);
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			task.setUser(user2);
			
			when(userService.getAuthenticatedUser()).thenReturn(user1);
			
			assertThrows(AccessDeniedException.class, () -> taskService.checkingTaskOwnership(task));
			
		}
	}
	
	@Nested
	class AddLinksToATask {
		
		@Test
		void addLinksToATask_ShouldAddAllLinks() {
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription("Description 1");
			
			TaskResponse result = taskService.addLinksToATask(taskResponse);
				
			assertThat(result.getLinks())
		        .extracting(link -> link.getRel().value(), link -> link.getHref())
		        .containsExactlyInAnyOrder(
		            Tuple.tuple("self", "/api/tasks/1"),
		            Tuple.tuple("update", "/api/tasks/1"),
		            Tuple.tuple("delete", "/api/tasks/1"),
		            Tuple.tuple("findAllTasks", "/api/tasks{?page,size}"),
		            Tuple.tuple("addSubtask", "/api/subtasks/1"),
		            Tuple.tuple("addTag", "/api/tags/add/1/{tagId}")
		    ); 
		}
	}
	
	@Nested
	class FindTaskOrThrow {
		
		@Test
		void findTaskOrThrow_ShouldReturnCorrectlyTask() {
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
			
			Task result = taskService.findTaskOrThrow(task.getId());
			
			assertThat(task).isEqualTo(result);
			verify(taskRepository, times(1)).findById(task.getId());
		}
		
		@Test
		void findTaskOrThrow_ShouldThrowBadRequestExceptionWhenGivenInvalidId() {
		
			BadRequestException ex = assertThrows(BadRequestException.class, () -> taskService.findTaskOrThrow(2L));
			
			assertThat(ex.getMessage()).isEqualTo("Task not found");
		}
	}
	
	@Nested
	class ApplyUpdatesToATask {
		
		@Test
		void applyUpdatesToATask_ShouldUpdateOnlyTitle() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .description("Description 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setTitle("Task 1 updated");
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(taskRequest.getTitle());
			
			assertThat(result.getDescription()).isEqualTo(task.getDescription());
			
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateOnlyDescription() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setDescription("Description 1 updated");
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			
			assertThat(result.getDescription()).isEqualTo(taskRequest.getDescription());
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateOnlyStatus() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setStatus(TaskStatus.DONE);
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			
			assertThat(result.getStatus()).isEqualTo(taskRequest.getStatus());
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateOnlyPriority() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setPriority(TaskPriority.HIGH);
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			
			assertThat(result.getPriority()).isEqualTo(taskRequest.getPriority());
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateOnlyDueDate() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setDueDate(LocalDate.of(2000, 01, 02));
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			
			assertThat(result.getDueDate()).isEqualTo(taskRequest.getDueDate());
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateTitleAndDescription() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setTitle("Task 1 updated");
			taskRequest.setDescription("Description 1 updated");
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(taskRequest.getTitle());
			assertThat(result.getDescription()).isEqualTo(taskRequest.getDescription());
		}
		
		@Test
		void applyUpdatesToATask_ShouldNotUpdateTitleWhenTitleIsBlank() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setTitle("");
			
			assertThrows(BadRequestException.class, 
					() -> taskService.applyUpdatesToATask(task, taskRequest));
		}
		
		@Test
		void applyUpdatesToATask_ShouldUpdateAllParams() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
			taskRequest.setTitle("Task 1 updated");
			taskRequest.setDescription("Description 1 updated");
			taskRequest.setStatus(TaskStatus.DONE);
			taskRequest.setPriority(TaskPriority.MEDIUM);
			taskRequest.setDueDate(LocalDate.of(2000, 01, 02));
			
			Task result = taskService.applyUpdatesToATask(task, taskRequest);
			
			assertThat(result.getTitle()).isEqualTo(taskRequest.getTitle());
			assertThat(result.getDescription()).isEqualTo(taskRequest.getDescription());
			assertThat(result.getStatus()).isEqualTo(taskRequest.getStatus());
			assertThat(result.getPriority()).isEqualTo(taskRequest.getPriority());
			assertThat(result.getDueDate()).isEqualTo(taskRequest.getDueDate());
		}
		
		@Test
		void applyUpdatesToATask_ShouldThrowBadRequestExceptionWhenNoUpdatesAreGiven() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			TaskUpdateRequest taskRequest = new TaskUpdateRequest();
	
			BadRequestException ex = assertThrows(BadRequestException.class, 
					() -> taskService.applyUpdatesToATask(task, taskRequest));
			
			assertThat(ex.getMessage()).isEqualTo("Please provide updates");
		}
		
	}
	
	@Nested
	class PrePersistTask {
		
		@Test
		void prePersistTask_ShouldSetupTaskCorrectly() {
			User user = new User(1L, "Joao", "password", Role.ROLE_USER);
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			when(userService.getAuthenticatedUser()).thenReturn(user);
			
			Task result = taskService.prePersistTask(task);
			
			assertThat(result.getId()).isEqualTo(task.getId());
			assertThat(result.getTitle()).isEqualTo(task.getTitle());
			
			assertThat(result.getUser()).isEqualTo(user);
			
			assertThat(result.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(result.getPriority()).isEqualTo(TaskPriority.NONE);
			assertThat(result.getCreatedDate()).isNotNull();
			
			verify(userService).getAuthenticatedUser();	
		}
		
		@Test
		void prePersistTask_ShouldThrowUserNotAuthenticatedExceptionWhenUserNotAuthenticated() {
			
			Task task = new Task.Builder()
	                .id(1L)
	                .title("Task 1")
	                .build();
			
			when(userService.getAuthenticatedUser()).thenThrow(new UserNotAuthenticatedException("User not authenticated"));
			
			UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () -> taskService.prePersistTask(task));
			
			assertThat(ex.getMessage()).isEqualTo("User not authenticated");
			assertThat(task.getUser()).isEqualTo(null);
			assertThat(task.getCreatedDate()).isNull();
			assertThat(task.getStatus()).isEqualTo(null);
			assertThat(task.getPriority()).isEqualTo(null);
		}
	}
		
}
