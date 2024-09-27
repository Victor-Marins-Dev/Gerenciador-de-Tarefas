package br.com.integrationtests;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.configs.TestConfig;
import br.com.dtos.TaskCreateRequest;
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
public class TaskIntegrationTestEdgeCases extends AbstractIntegrationTest{

	private RequestSpecification specification;
	private ObjectMapper objectMapper;
	private AuthTestUtil auth;
	
	private TaskCreateRequest taskCreateRequestFromUser1 = new TaskCreateRequest();
	private TaskCreateRequest taskCreateRequestFromUser2 = new TaskCreateRequest();
	
	private Long user1TaskId;
	private Long user2TaskId;
	
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
		
		taskCreateRequestFromUser1.setTitle("Task 1");
		taskCreateRequestFromUser1.setDescription("Description 1");
		taskCreateRequestFromUser1.setPriority(TaskPriority.MEDIUM);
		taskCreateRequestFromUser1.setStatus(TaskStatus.UNDONE);
		
		taskCreateRequestFromUser2.setTitle("Task 1");
		taskCreateRequestFromUser2.setDescription("Description 1");
		taskCreateRequestFromUser2.setPriority(TaskPriority.HIGH);
		taskCreateRequestFromUser2.setDueDate(LocalDate.of(2050, 12, 12));
		
		user1TaskId = 1L;
		
		user2TaskId = 2L;
		
		auth = new AuthTestUtil(objectMapper);
	}
	
	@Test
	@Order(1)
	void populatingDB() throws Exception {
		
		auth.createUser("user1");
		auth.createUser("user2");
		
		var tokenUser1 = auth.loggingUser("user1");
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .header("Authorization", "Bearer " + tokenUser1)
	        .body(objectMapper.writeValueAsString(taskCreateRequestFromUser1))
	    .when()
	    	.post()
	    .then()
	    	.statusCode(201);
		
		var tokenUser2 = auth.loggingUser("user2");
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .header("Authorization", "Bearer " + tokenUser2)
	        .body(objectMapper.writeValueAsString(taskCreateRequestFromUser2))
	    .when()
	    	.post()
	    .then()
	    	.statusCode(201);
		
	}
	
	@Test
	@Order(2)
	void create_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws JsonProcessingException {
		
		TaskCreateRequest request = new TaskCreateRequest();
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .body(objectMapper.writeValueAsString(request))
	    .when()
	    	.post()
	    .then()
	    	.statusCode(401);
		
	}
	
	@Test
	@Order(3)
	void create_ShouldReturnBadRequestWhenBodyIsNull() throws Exception {
		
		String token = auth.loggingUser("user1");
		
		given()
	        .spec(specification)
	        .header("Authorization", "Bearer " + token)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
        .when()
        	.post()
        .then()
        	.statusCode(400);
		
	}
	
	@Test
	@Order(4)
	void create_ShouldReturnBadRequestWhenTitleIsNull() throws Exception {
		
		String token = auth.loggingUser("user1");
		
		TaskCreateRequest request = new TaskCreateRequest();
		request.setDescription("xdxd");
		
		given()
	        .spec(specification)
	        .header("Authorization", "Bearer " + token)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .body(objectMapper.writeValueAsString(request))
        .when()
        	.post()
        .then()
        	.statusCode(400);
		
	}
	
	@Test
	@Order(5)
	void create_ShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
		
		String token = auth.loggingUser("user1");
		
		TaskCreateRequest request = new TaskCreateRequest();
		request.setTitle("");
		
		given()
	        .spec(specification)
	        .header("Authorization", "Bearer " + token)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .body(objectMapper.writeValueAsString(request))
        .when()
        	.post()
        .then()
        	.statusCode(400);
		
	}
	
	@Test
	@Order(6)
	void create_ShouldReturnBadRequestWhenDescriptionIsTooBig() throws Exception {
		
		String token = auth.loggingUser("user1");
		
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
		
		TaskCreateRequest request = new TaskCreateRequest();
		request.setTitle("XdXd");
		request.setDescription(overFiveHundredString.toString());
		
		given()
	        .spec(specification)
	        .header("Authorization", "Bearer " + token)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .body(objectMapper.writeValueAsString(request))
        .when()
        	.post()
        .then()
        	.statusCode(400);
		
	}
	
	@Test
	@Order(7)
	void create_ShouldReturnBadRequestWhenDueDateIsInThePast() throws Exception {
		
		String token = auth.loggingUser("user1");
		
		TaskCreateRequest request = new TaskCreateRequest();
		request.setTitle("xDxD");
		request.setDueDate(LocalDate.of(1990, 12, 12));
		
		given()
	        .spec(specification)
	        .header("Authorization", "Bearer " + token)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .body(objectMapper.writeValueAsString(request))
        .when()
        	.post()
        .then()
        	.statusCode(400);
		
	}
	
	@Test
	@Order(8)
	void findById_ShouldReturnUnauthorizedWhenUserNotAuthenticated() {
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	    .when()
	    	.get("/{taskId}", 1L)
	    .then()
	    	.statusCode(401);
		
	}
	
	@Test
	@Order(9)
	void findById_ShouldReturnForbiddenWhenAnUserTryToAccessATaskThatNotBelongToHim() throws Exception {
		
		String token = auth.loggingUser("user1");
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get("/{taskId}", user2TaskId)
	        .then()
	        	.statusCode(403);
	}
	
	@Test
	@Order(10)
	void findById_ShouldReturnBadRequestWhenTaskNotFound() throws Exception {
		
		String token = auth.loggingUser("user1");
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.get("/{taskId}", 500L)
	        .then()
	        	.statusCode(400);
	}
	
	@Test
	@Order(11)
	void findAllByUserAuthenticated_ShouldReturnUnauthorizedWhenUserNotAuthenticated() {
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	    .when()
	    	.get()
	    .then()
	    	.statusCode(401);
	}
	
	@Test
	@Order(12)
	void customizedSearch_ShouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
		
			given()
		        .spec(specification)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .param("status", "UNDONE")
		        .param("priority", "MEDIUM")
	        .when()
	        	.get("/search")
	        .then()
	        	.statusCode(401);
	}
	
	@Test
	@Order(13)
	void partialUpdate_ShouldReturnBadRequestWhenDescriptionIsToBig() throws Exception {
	
		String token = auth.loggingUser("user1");
		
		StringBuilder over500CharsString = new StringBuilder();
		over500CharsString
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
		taskUpdateRequest.setTitle("Task 1 updated");
		taskUpdateRequest.setDescription(over500CharsString.toString());
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskUpdateRequest))
	        .when()
	        	.patch("/{taskId}", user1TaskId)
	        .then()
	        	.statusCode(400);
	}
	
	@Test
	@Order(14)
	void partialUpdate_ShouldReturnBadRequestWhenDueDateIsInThePast() throws Exception {
	
		String token = auth.loggingUser("user1");
	
		TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
		taskUpdateRequest.setDueDate(LocalDate.of(2001, 03, 05));
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskUpdateRequest))
	        .when()
	        	.patch("/{taskId}", user1TaskId)
	        .then()
	        	.statusCode(400);
	}
	
	@Test
	@Order(15)
	void partialUpdate_ShouldReturnForbiddenWhenAnUserTryToUpdateATaskThatNotBelongToHim() throws Exception {

		String token = auth.loggingUser("user1");
		
		TaskUpdateRequest taskUpdateRequest = new TaskUpdateRequest();
		taskUpdateRequest.setTitle("Task 1 updated");
		taskUpdateRequest.setDescription("updated ^^");
		taskUpdateRequest.setPriority(TaskPriority.NONE);
		taskUpdateRequest.setStatus(TaskStatus.DONE);
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
		        .body(objectMapper.writeValueAsString(taskUpdateRequest))
	        .when()
	        	.patch("/{taskId}", user2TaskId)
	        .then()
	        	.statusCode(403); 		
	}
	
	@Test
	@Order(16)
	void partialUpdate_ShouldReturnUnauthorizedWhenUserNotAuthenticated() {
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	    .when()
	    	.patch("/{taskId}", user1TaskId)
	    .then()
	    	.statusCode(401);
		
	}
	
	@Test
	@Order(17)
	void delete_ShouldReturnForbiddenWhenAnUserTryToDeleteATaskThatNotBelongToHim() throws Exception {
	
		String token = auth.loggingUser("user1");
		
			given()
		        .spec(specification)
		        .header("Authorization", "Bearer " + token)
		        .contentType(TestConfig.CONTENT_TYPE_JSON)
	        .when()
	        	.delete("/{taskId}", user2TaskId)
	        .then()
	        	.statusCode(403);
	}
	
	@Test
	@Order(18)
	void delete_ShouldReturnUnauthorizedWhenUserNotAuthenticated() {
		
		given()
	        .spec(specification)
	        .contentType(TestConfig.CONTENT_TYPE_JSON)
	    .when()
	    	.delete("/{taskId}", user1TaskId)
	    .then()
	    	.statusCode(401);
		
	}
	
}
