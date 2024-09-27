package br.com.integrationtests;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.configs.TestConfig;
import br.com.dtos.TaskCreateRequest;
import br.com.dtos.TaskResponse;
import br.com.dtos.TaskUpdateRequest;
import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import br.com.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.utils.AuthTestUtil;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskIntegrationTestHappyPath extends AbstractIntegrationTest{
	
	private RequestSpecification specification;
	private ObjectMapper objectMapper;
	private AuthTestUtil auth;
	
	private TaskCreateRequest taskCreateRequestFromCarlin = new TaskCreateRequest();
	private TaskCreateRequest taskCreateRequestFromAugustin = new TaskCreateRequest();
	private Long carlinFirstTaskId;
	private Long augustinFirstTaskId;
	
	private Map<String, Set<Long>> usernameToIdsMap = new HashMap<>();
	
	@Autowired
	private Flyway flyway;
	
	@BeforeAll
	public void setUp() throws Exception {
		
		flyway.clean();
        flyway.migrate();
		
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new JavaTimeModule());
		
		specification = new RequestSpecBuilder()
				.setBasePath("/api/tasks")
				.setPort(TestConfig.SERVER_PORT)
				.addFilter(new RequestLoggingFilter(LogDetail.ALL))
				.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		taskCreateRequestFromCarlin.setTitle("Task 1 from carlin");
		taskCreateRequestFromCarlin.setDescription("Description 1 from carlin");
		taskCreateRequestFromCarlin.setPriority(TaskPriority.MEDIUM);
		taskCreateRequestFromCarlin.setStatus(TaskStatus.UNDONE);
		
		taskCreateRequestFromAugustin.setTitle("Task 1 from augustin");
		taskCreateRequestFromAugustin.setDescription("Description 1 from augustin");
		taskCreateRequestFromAugustin.setPriority(TaskPriority.HIGH);
		taskCreateRequestFromAugustin.setDueDate(LocalDate.of(2050, 12, 12));
		
		carlinFirstTaskId = 1L;
		
		augustinFirstTaskId = 2L;
		
		usernameToIdsMap.putIfAbsent("carlin", new HashSet<>());
		usernameToIdsMap.get("carlin").add(carlinFirstTaskId);
		
		usernameToIdsMap.putIfAbsent("augustin", new HashSet<>());
        usernameToIdsMap.get("augustin").add(augustinFirstTaskId);
        
        auth = new AuthTestUtil(objectMapper);
	
	}
	
	private void populatingDataBaseWithMore5Tasks(String username) throws JsonProcessingException {
		
		String token = auth.loggingUser(username);
		
		TaskCreateRequest request1 = new TaskCreateRequest();
		request1.setTitle("Task A");
		request1.setDescription("Description A");
		request1.setPriority(TaskPriority.MEDIUM);
		request1.setStatus(TaskStatus.UNDONE);
		
		TaskCreateRequest request2 = new TaskCreateRequest();
		request2.setTitle("Task B");
		request2.setDescription("Description B");
		request2.setPriority(TaskPriority.LOW);
		
		TaskCreateRequest request3 = new TaskCreateRequest();
		request3.setTitle("Task C");
		request3.setDescription("Description C");
		request3.setPriority(TaskPriority.HIGH);
		
		TaskCreateRequest request4 = new TaskCreateRequest();
		request4.setTitle("Task D");
		request4.setDescription("Description D");
		request4.setPriority(TaskPriority.MEDIUM);
		request4.setStatus(TaskStatus.DONE);
		
		TaskCreateRequest request5 = new TaskCreateRequest();
		request5.setTitle("Task E");
		request5.setDescription("Description E");
		request5.setDueDate(LocalDate.of(2030, 12, 12));
		
		List<TaskCreateRequest> requests = Arrays.asList(request1, request2, request3, request4, request5);
		
		for (TaskCreateRequest request : requests) {
			String responseBody = 
					given()
				        .spec(specification)
				        .header("Authorization", "Bearer " + token)
				        .contentType(TestConfig.CONTENT_TYPE_JSON)
				        .body(objectMapper.writeValueAsString(request))
			        .when()
			        	.post()
			        .then()
			        	.statusCode(201)
			        	.extract()
			        	.body()
			        	.asString();
			
			TaskResponse taskResponse = objectMapper.readValue(responseBody, TaskResponse.class);
			
			usernameToIdsMap.get(username).add(taskResponse.getId());
		}		
	}
	
	
	@Test
	@Order(1)
	void create_ShouldReturnATaskResponseWhenCreateATaskFromCarlin() throws Exception {
		auth.createUser("carlin");
		String token = auth.loggingUser("carlin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskCreateRequestFromCarlin))
	        .when()
	        	.post()
	        .then()
	        	.statusCode(201)
	        	.extract()
	        	.body()
	        	.asString();
		
		TaskResponse taskResponse = objectMapper.readValue(responseBody, TaskResponse.class);
		
		assertNotNull(taskResponse);
		assertNotNull(taskResponse.getLinks());
		assertNotNull(taskResponse.getCreatedDate());
		assertNotNull(taskResponse.getStatus());
		assertThat(taskResponse.getTitle()).isEqualTo(taskCreateRequestFromCarlin.getTitle());
		assertThat(taskResponse.getDescription()).isEqualTo(taskCreateRequestFromCarlin.getDescription());
		assertThat(taskResponse.getPriority()).isEqualTo(taskCreateRequestFromCarlin.getPriority());	
	}
	
	@Test
	@Order(2)
	void create_ShouldReturnATaskResponseWhenCreateATaskFromAugustin() throws Exception {
		
		auth.createUser("augustin");
		String token = auth.loggingUser("augustin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskCreateRequestFromAugustin))
	        .when()
	        	.post()
	        .then()
	        	.statusCode(201)
	        	.extract()
	        	.body()
	        	.asString();
		
		TaskResponse taskResponse = objectMapper.readValue(responseBody, TaskResponse.class);
		
		assertNotNull(taskResponse);
		assertNotNull(taskResponse.getLinks());
		assertNotNull(taskResponse.getCreatedDate());
		assertNotNull(taskResponse.getStatus());
		assertThat(taskResponse.getTitle()).isEqualTo(taskCreateRequestFromAugustin.getTitle());
		assertThat(taskResponse.getDescription()).isEqualTo(taskCreateRequestFromAugustin.getDescription());
		assertThat(taskResponse.getPriority()).isEqualTo(taskCreateRequestFromAugustin.getPriority());	
		assertThat(taskResponse.getDueDate()).isEqualTo(taskCreateRequestFromAugustin.getDueDate());	
	}
	
	@Test
	@Order(3)
	void findById_ShouldReturnATaskResponseFromCarlin() throws Exception {
		
		String token = auth.loggingUser("carlin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get("/{taskId}", carlinFirstTaskId)
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		TaskResponse taskResponse = objectMapper.readValue(responseBody, TaskResponse.class);
		
		assertNotNull(taskResponse);
		assertNotNull(taskResponse.getLinks());
		assertNotNull(taskResponse.getCreatedDate());
		assertNotNull(taskResponse.getStatus());
		assertThat(taskResponse.getTitle()).isEqualTo(taskCreateRequestFromCarlin.getTitle());
		assertThat(taskResponse.getDescription()).isEqualTo(taskCreateRequestFromCarlin.getDescription());
		assertThat(taskResponse.getPriority()).isEqualTo(taskCreateRequestFromCarlin.getPriority());	
	}
	
	@Test
	@Order(4)
	void findById_ShouldReturnATaskResponseFromAugustin() throws Exception {
		
		String token = auth.loggingUser("augustin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get("/{taskId}", augustinFirstTaskId)
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		TaskResponse taskResponse = objectMapper.readValue(responseBody, TaskResponse.class);
		
		assertNotNull(taskResponse);
		assertNotNull(taskResponse.getLinks());
		assertNotNull(taskResponse.getCreatedDate());
		assertNotNull(taskResponse.getStatus());
		assertThat(taskResponse.getTitle()).isEqualTo(taskCreateRequestFromAugustin.getTitle());
		assertThat(taskResponse.getDescription()).isEqualTo(taskCreateRequestFromAugustin.getDescription());
		assertThat(taskResponse.getPriority()).isEqualTo(taskCreateRequestFromAugustin.getPriority());	
	}
	
	@Test
	@Order(5)
	void findAllByUserAuthenticated_ShouldReturnOnlyTasksFromCarlin() throws Exception {
		
		populatingDataBaseWithMore5Tasks("carlin");	
		
		String token = auth.loggingUser("carlin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get()
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		
		 PagedModel<EntityModel<TaskResponse>> pagedModel = 
			        objectMapper.readValue(responseBody, new TypeReference<PagedModel<EntityModel<TaskResponse>>>() {});
		 
		 List<TaskResponse> listTaskResponse = pagedModel.getContent().stream()
				    .map(EntityModel::getContent)
				    .collect(Collectors.toList());
		
		assertNotNull(pagedModel);
		assertNotNull(pagedModel.getLinks());
		
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(usernameToIdsMap.get("augustin")).doesNotContain(taskResponse.getId());
		}
		
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(usernameToIdsMap.get("carlin")).contains(taskResponse.getId());
		}
	}
	
	@Test
	@Order(6)
	void findAllByUserAuthenticated_ShouldReturnOnlyTasksFromAugustin() throws Exception {
		
		populatingDataBaseWithMore5Tasks("augustin");
		
		String token = auth.loggingUser("augustin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get()
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		
		 PagedModel<EntityModel<TaskResponse>> pagedModel = 
			        objectMapper.readValue(responseBody, new TypeReference<PagedModel<EntityModel<TaskResponse>>>() {});
		 
		 List<TaskResponse> listTaskResponse = pagedModel.getContent().stream()
				    .map(EntityModel::getContent)
				    .collect(Collectors.toList());
		
		assertNotNull(pagedModel);
		assertNotNull(pagedModel.getLinks());
		
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(usernameToIdsMap.get("carlin")).doesNotContain(taskResponse.getId());
		}
		
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(usernameToIdsMap.get("augustin")).contains(taskResponse.getId());
		}
	}
	
	@Test
	@Order(7)
	void customizedSearch_ShouldReturnOnlyTasksFromCarlin() throws Exception {
	
		String token = auth.loggingUser("carlin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .param("status", "UNDONE")
		        .param("priority", "MEDIUM")
	        .when()
	        	.get("/search")
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		
		 PagedModel<EntityModel<TaskResponse>> pagedModel = 
			        objectMapper.readValue(responseBody, new TypeReference<PagedModel<EntityModel<TaskResponse>>>() {});
		 
		 List<TaskResponse> listTaskResponse = pagedModel.getContent().stream()
				    .map(EntityModel::getContent)
				    .collect(Collectors.toList());
		
		assertNotNull(pagedModel);
		assertNotNull(pagedModel.getLinks());
		assertThat(pagedModel.getMetadata().getTotalElements()).isEqualTo(2);
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(taskResponse.getPriority()).isEqualTo(TaskPriority.MEDIUM);
			assertThat(taskResponse.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(usernameToIdsMap.get("carlin")).contains(taskResponse.getId());
		}
	}
	
	@Test
	@Order(8)
	void customizedSearch_ShouldReturnOnlyTasksFromAugustin() throws Exception {
	
		String token = auth.loggingUser("augustin");
		
		String responseBody = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .param("status", "UNDONE")
		        .param("priority", "MEDIUM")
	        .when()
	        	.get("/search")
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		
		 PagedModel<EntityModel<TaskResponse>> pagedModel = 
			        objectMapper.readValue(responseBody, new TypeReference<PagedModel<EntityModel<TaskResponse>>>() {});
		 
		 List<TaskResponse> listTaskResponse = pagedModel.getContent().stream()
				    .map(EntityModel::getContent)
				    .collect(Collectors.toList());
		
		assertNotNull(pagedModel);
		assertNotNull(pagedModel.getLinks());
		assertThat(pagedModel.getMetadata().getTotalElements()).isEqualTo(1);
		for (TaskResponse taskResponse : listTaskResponse) {
			assertThat(taskResponse.getPriority()).isEqualTo(TaskPriority.MEDIUM);
			assertThat(taskResponse.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(usernameToIdsMap.get("augustin")).contains(taskResponse.getId());
		}
	}
	
	@Test
	@Order(9)
	void partialUpdate_ShouldUpdateCorrectlyAnTask() throws Exception {
	
		String token = auth.loggingUser("carlin");
		
		TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
		taskUpdateRequest.setTitle("Task 1 from carlin updated");
		taskUpdateRequest.setDescription("updated");
		taskUpdateRequest.setPriority(TaskPriority.NONE);
		taskUpdateRequest.setStatus(TaskStatus.DONE);
		
		String responseBodyFromUpdate = 
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskUpdateRequest))
	        .when()
	        	.patch("{taskId}", carlinFirstTaskId)
	        .then()
	        	.statusCode(200)
	        	.extract()
	        	.body()
	        	.asString();
		
		
		TaskResponse taskResponseFromUpdate = objectMapper.readValue(responseBodyFromUpdate, TaskResponse.class);
		 
		assertNotNull(taskResponseFromUpdate);
		assertNotNull(taskResponseFromUpdate.getLinks());
		assertThat(taskResponseFromUpdate.getTitle()).isEqualTo(taskUpdateRequest.getTitle());
		assertThat(taskResponseFromUpdate.getDescription()).isEqualTo(taskUpdateRequest.getDescription());
		assertThat(taskResponseFromUpdate.getPriority()).isEqualTo(taskUpdateRequest.getPriority());
		assertThat(taskResponseFromUpdate.getStatus()).isEqualTo(taskUpdateRequest.getStatus());
		
		String responseBodyFromFindById = 
				given()
			        .spec(specification)
			        .header("Authorization", "Bearer " + token)
			        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .when()
		        	.get("/{taskId}", carlinFirstTaskId)
		        .then()
		        	.statusCode(200)
		        	.extract()
		        	.body()
		        	.asString();
		
		TaskResponse taskResponseFromFindById = objectMapper.readValue(responseBodyFromFindById, TaskResponse.class);
		
		assertNotNull(taskResponseFromFindById);
		assertNotNull(taskResponseFromFindById.getLinks());
		assertThat(taskResponseFromFindById.getTitle()).isEqualTo(taskUpdateRequest.getTitle());
		assertThat(taskResponseFromFindById.getDescription()).isEqualTo(taskUpdateRequest.getDescription());
		assertThat(taskResponseFromFindById.getPriority()).isEqualTo(taskUpdateRequest.getPriority());
		assertThat(taskResponseFromFindById.getStatus()).isEqualTo(taskUpdateRequest.getStatus());
		
	}
	
	@Test
	@Order(10)
	void delete_ShouldReturnNoContentWhenDeleteATask() throws Exception {
	
		String token = auth.loggingUser("carlin");
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.delete("/{taskId}", carlinFirstTaskId)
	        .then()
	        	.statusCode(204);
	}
	
}
