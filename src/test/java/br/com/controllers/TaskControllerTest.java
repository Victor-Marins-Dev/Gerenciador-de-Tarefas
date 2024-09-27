package br.com.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.dtos.TaskCreateRequest;
import br.com.dtos.TaskResponse;
import br.com.dtos.TaskUpdateRequest;
import br.com.exceptions.BadRequestException;
import br.com.exceptions.handler.ApiExceptionHandler;
import br.com.services.TaskService;

@WebMvcTest
@ContextConfiguration(classes = {TaskController.class})
@Import(ApiExceptionHandler.class)
public class TaskControllerTest {

	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;
    
	private Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
    
    @Nested
    class FindById {
    	
    	@Test
        @WithMockUser(username = "user", roles = {"USER"})
        void findById_ShouldReturnTaskCorrectly() throws Exception {
        	
            TaskResponse task = new TaskResponse();
            task.setId(1L);
            task.setTitle("Task 1");
            
            when(taskService.findById(1L)).thenReturn(task);

            mockMvc.perform(get("/api/tasks/{taskId}", task.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(task.getId()))
                    .andExpect(jsonPath("$.title").value(task.getTitle()));
        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        void findById_ShouldReturnBadRequestWhenTaskNotFound() throws Exception {
        	 
            when(taskService.findById(1L)).thenThrow(new BadRequestException("Task not found"));

            mockMvc.perform(get("/api/tasks/{taskId}", 1L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Task not found"));

        }
        
        @Test
        @WithMockUser(username = "user", roles = {"USER"})
        void findById_ShouldReturnForbiddenWhenTaskDoesntBelongToTheUser() throws Exception {
        	 
            when(taskService.findById(1L)).thenThrow(new AccessDeniedException("Task doesn't belong to the user"));

            mockMvc.perform(get("/api/tasks/{taskId}", 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Task doesn't belong to the user"));

        }
        
        @Test
        void findById_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
        	
            mockMvc.perform(get("/api/tasks/{taskId}", 1L))
                    .andExpect(status().isUnauthorized());

        }
    }
    
	@Nested
	class FindAllByUserAuthenticated {
		
		@Test
        @WithMockUser(username = "user", roles = {"USER"})
        void findAllByUserAuthenticated_ShouldReturnPagedModelCorrectly() throws Exception {
        	
            TaskResponse task1 = new TaskResponse();
            task1.setId(1L);
            task1.setTitle("Task 1");
            
            TaskResponse task2 = new TaskResponse();
            task2.setId(2L);
            task2.setTitle("Task 2");
            
            List<TaskResponse> list = Arrays.asList(task1, task2);
            List<EntityModel<TaskResponse>> entityModelList = list.stream().map(EntityModel::of).toList();
            
            PagedModel<EntityModel<TaskResponse>> mockPagedModel = PagedModel.of(
                entityModelList, new PagedModel.PageMetadata(2, 0, 2, 1)); 
            
            when(taskService.findAllByUserAuthenticated(pageable)).thenReturn(mockPagedModel);

            mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.taskResponseList.size()").value(2))
            .andExpect(jsonPath("$._embedded.taskResponseList[0].title").value("Task 1"))
            .andExpect(jsonPath("$._embedded.taskResponseList[1].title").value("Task 2"));
        }
		
		@Test
        void findAllByUserAuthenticated_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
			
			mockMvc.perform(get("/api/tasks"))
	        .andExpect(status().isUnauthorized());
        }
	}
	
	@Nested
	class CustomizedSearch {
		
		@Test
        @WithMockUser(username = "user", roles = {"USER"})
        void customizedSearch_ShouldReturnPagedModelCorrectly() throws Exception {
        	
            TaskResponse task1 = new TaskResponse();
            task1.setId(1L);
            task1.setTitle("Task 1");
            
            TaskResponse task2 = new TaskResponse();
            task2.setId(2L);
            task2.setTitle("Task 2");
            
            List<TaskResponse> list = Arrays.asList(task1, task2);
            List<EntityModel<TaskResponse>> entityModelList = list.stream().map(EntityModel::of).toList();
            
            PagedModel<EntityModel<TaskResponse>> mockPagedModel = PagedModel.of(
                entityModelList, new PagedModel.PageMetadata(2, 0, 2, 1)); 
            
            when(taskService.customizedSearch(null, null, null, pageable)).thenReturn(mockPagedModel);

            mockMvc.perform(get("/api/tasks/search"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.taskResponseList.size()").value(2))
            .andExpect(jsonPath("$._embedded.taskResponseList[0].title").value("Task 1"))
            .andExpect(jsonPath("$._embedded.taskResponseList[1].title").value("Task 2"));
        }
		
		@Test
        @WithMockUser(username = "user", roles = {"USER"})
        void customizedSearch_ShouldReturnPagedModelCorrectlyWhenGivenParams() throws Exception {
        	
            TaskResponse task1 = new TaskResponse();
            task1.setId(1L);
            task1.setTitle("Task 1");
            
            TaskResponse task2 = new TaskResponse();
            task2.setId(2L);
            task2.setTitle("Task 2");
            
            List<TaskResponse> list = Arrays.asList(task1, task2);
            List<EntityModel<TaskResponse>> entityModelList = list.stream().map(EntityModel::of).toList();
            
            PagedModel<EntityModel<TaskResponse>> mockPagedModel = PagedModel.of(
                entityModelList, new PagedModel.PageMetadata(2, 0, 2, 1)); 
            
            when(taskService.customizedSearch(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(mockPagedModel);
            
            mockMvc.perform(get("/api/tasks/search")
                    .param("status", "done")
                    .param("priority", "low")
                    .param("tagName", "study")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.taskResponseList").isArray())
                    .andExpect(jsonPath("$._embedded.taskResponseList.size()").value(2));
        }
		
		@Test
        void customizedSearch_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {

            mockMvc.perform(get("/api/tasks/search")
                    .param("status", "done")
                    .param("priority", "low")
                    .param("tagName", "study")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isUnauthorized());
        }
	}
	
	@Nested
	class Create {
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void create_ShouldReturnIsCreatedAndCorrectlyTaskResponse() throws JsonProcessingException, Exception {
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			taskCreateRequest.setTitle("Task 1");
			taskCreateRequest.setDescription("Description 1");
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription("Description 1");
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isCreated())
			        .andExpect(jsonPath("$.id").value(1L))
			        .andExpect(jsonPath("$.title").value("Task 1"))
			        .andExpect(jsonPath("$.description").value("Description 1"));
			
			verify(taskService, times(1)).create(any(TaskCreateRequest.class));
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void create_ShouldReturnBadRequestWhenTaskCreateRequestIsNull() throws JsonProcessingException, Exception {
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			
			TaskResponse taskResponse = new TaskResponse();
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			
			verify(taskService, never()).create(any(TaskCreateRequest.class));
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void create_ShouldReturnBadRequestWhenTitleIsBlank() throws JsonProcessingException, Exception {
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			taskCreateRequest.setDescription("AAA");
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setDescription("AAA");
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			
			verify(taskService, never()).create(any(TaskCreateRequest.class));
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void create_ShouldReturnBadRequestWhenDescriptionIsTooLong() throws JsonProcessingException, Exception {
			
			StringBuilder overFiveHundredString = new StringBuilder();
			overFiveHundredString
				.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
			    .append("Sed do eiusmod tempor incididunt ut labore et dolore ")
			    .append("magna aliqua. Ut enim ad minim veniam, quis nostrud ")
			    .append("exercitation ullamco laboris nisi ut aliquip ex ea ")
			    .append("commodo consequat. Duis aute irure dolor in reprehenderit ")
			    .append("in voluptate velit esse cillum dolore eu fugiat nulla ")
			    .append("pariatur. Excepteur sint occaecat cupidatat non proident, ")
			    .append("sunt in culpa qui officia deserunt mollit anim id est laborum. ")
			    .append("Curabitur pretium tincidunt lacus. Nulla gravida orci a odio, ")
			    .append("dignissim quis lacinia.1");
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			taskCreateRequest.setTitle("Task 1");
			taskCreateRequest.setDescription(overFiveHundredString.toString());
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription(overFiveHundredString.toString());
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			
			verify(taskService, never()).create(any(TaskCreateRequest.class));
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void create_ShouldReturnBadRequestWhenDueDateIsInThePast() throws JsonProcessingException, Exception {
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			taskCreateRequest.setTitle("Task 1");
			taskCreateRequest.setDueDate(LocalDate.of(1990, 05, 12));
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDueDate(LocalDate.of(1990, 05, 12));
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			
			verify(taskService, never()).create(any(TaskCreateRequest.class));
		}
		
		@Test
		void create_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws JsonProcessingException, Exception {
			
			TaskCreateRequest taskCreateRequest = new TaskCreateRequest();
			taskCreateRequest.setTitle("Task 1");
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			
			when(taskService.create(any(TaskCreateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(post("/api/tasks")
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskCreateRequest))
			        .with(csrf()))
			        .andExpect(status().isUnauthorized());

			verify(taskService, never()).create(any(TaskCreateRequest.class));
		}	
	}
	
	@Nested
	class PartialUpdate {
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnOkAndCorrectlyTaskResponse() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setTitle("Task 1");
			taskUpdateRequest.setDescription("Description 1");
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription("Description 1");
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isOk())
			        .andExpect(jsonPath("$.id").value(1L))
			        .andExpect(jsonPath("$.title").value("Task 1"))
			        .andExpect(jsonPath("$.description").value("Description 1"));
			
			verify(taskService).partialUpdate(anyLong(), any(TaskUpdateRequest.class));
			
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnBadRequestWhenTaskUpdateRequestIsNull() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenThrow(new BadRequestException("Please provide updates"));
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());
		
			verify(taskService).partialUpdate(anyLong(), any(TaskUpdateRequest.class));
			
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnBadRequestWhenDescriptionIsTooLong() throws JsonProcessingException, Exception {
			
			StringBuilder overFiveHundredString = new StringBuilder();
			overFiveHundredString
				.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
			    .append("Sed do eiusmod tempor incididunt ut labore et dolore ")
			    .append("magna aliqua. Ut enim ad minim veniam, quis nostrud ")
			    .append("exercitation ullamco laboris nisi ut aliquip ex ea ")
			    .append("commodo consequat. Duis aute irure dolor in reprehenderit ")
			    .append("in voluptate velit esse cillum dolore eu fugiat nulla ")
			    .append("pariatur. Excepteur sint occaecat cupidatat non proident, ")
			    .append("sunt in culpa qui officia deserunt mollit anim id est laborum. ")
			    .append("Curabitur pretium tincidunt lacus. Nulla gravida orci a odio, ")
			    .append("dignissim quis lacinia.1");
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setDescription(overFiveHundredString.toString());
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription(overFiveHundredString.toString());
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			verify(taskService, never()).partialUpdate(anyLong(), any(TaskUpdateRequest.class));	
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnBadRequestWhenDueDateIsInThePast() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setDueDate(LocalDate.of(2000, 01, 01));
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription("Description 1");
			taskResponse.setDueDate(LocalDate.of(2000, 01, 01));
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			verify(taskService, never()).partialUpdate(anyLong(), any(TaskUpdateRequest.class));	
		}
		
		@Test
		void partialUpdate_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setTitle("Task 1");
			taskUpdateRequest.setDescription("Description 1");
			
			TaskResponse taskResponse = new TaskResponse();
			taskResponse.setId(1L);
			taskResponse.setTitle("Task 1");
			taskResponse.setDescription("Description 1");

			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenReturn(taskResponse);
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isUnauthorized());

			verify(taskService, never()).partialUpdate(anyLong(), any(TaskUpdateRequest.class));	
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnBadRequestWhenTaskNotFound() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setTitle("Task 1");
			taskUpdateRequest.setDescription("Description 1");
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenThrow(new BadRequestException("Task not found"));
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isBadRequest());

			verify(taskService).partialUpdate(anyLong(), any(TaskUpdateRequest.class));	
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void partialUpdate_ShouldReturnForbiddenWhenTaskDoesntBelongToTheUser() throws JsonProcessingException, Exception {
			
			TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
			taskUpdateRequest.setTitle("Task 1");
			taskUpdateRequest.setDescription("Description 1");
			
			when(taskService.partialUpdate(anyLong(), any(TaskUpdateRequest.class))).thenThrow(new AccessDeniedException("Task doesn't belong to the user"));
				
			mockMvc.perform(patch("/api/tasks/{taskId}", 1L)
			        .contentType(MediaType.APPLICATION_JSON)
			        .content(objectMapper.writeValueAsString(taskUpdateRequest))
			        .with(csrf()))
			        .andExpect(status().isForbidden());

			verify(taskService).partialUpdate(anyLong(), any(TaskUpdateRequest.class));	
		}	
	}
	
	@Nested
	class Delete {
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void delete_ShouldReturnNoContentWhenEverythingOk() throws JsonProcessingException, Exception {
			
			doNothing().when(taskService).delete(anyLong());
			
			 mockMvc.perform(delete("/api/tasks/{taskId}", 1L)
			            .with(csrf()))
			            .andExpect(status().isNoContent());
			        
			verify(taskService).delete(anyLong());
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void delete_ShouldReturnBadRequestWhenTaskNotFound() throws JsonProcessingException, Exception {
			
			doThrow(new BadRequestException("Task not found")).when(taskService).delete(anyLong());
			
			 mockMvc.perform(delete("/api/tasks/{taskId}", 1L)
			            .with(csrf()))
			            .andExpect(status().isBadRequest());
			        
			verify(taskService).delete(anyLong());
		}
		
		@Test
		@WithMockUser(username = "user", roles = {"USER"})
		void delete_ShouldReturnForbiddenWhenTaskDoesntBelongToTheUser() throws JsonProcessingException, Exception {
			
			doThrow(new AccessDeniedException("Task doesn't belong to the user")).when(taskService).delete(anyLong());
			
			 mockMvc.perform(delete("/api/tasks/{taskId}", 1L)
			            .with(csrf()))
			            .andExpect(status().isForbidden());
			        
			verify(taskService).delete(anyLong());
		}
		
		@Test
		void delete_ShouldReturnForbiddenWhenTaskDoesntBelongToTheUsera() throws JsonProcessingException, Exception {
			
			 mockMvc.perform(delete("/api/tasks/{taskId}", 1L)
			            .with(csrf()))
			            .andExpect(status().isUnauthorized());
			        
			verify(taskService, never()).delete(anyLong());
		}
	}
	
}
