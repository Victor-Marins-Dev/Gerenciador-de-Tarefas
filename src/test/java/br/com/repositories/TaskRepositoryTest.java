package br.com.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.enums.Role;
import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import br.com.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.models.Tag;
import br.com.models.Task;
import br.com.models.User;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest extends AbstractIntegrationTest{
	
	@Autowired
	TaskRepository taskRepository;
	
	@Autowired
	TagRepository tagRepository;
	
	@Autowired
	UserRepository userRepository;
	
	private User user1;
	private User user2;
	
    private Task taskAFromUser1;
    private Task taskBFromUser1;
    private Task taskAFromUser2;
    private Task taskBFromUser2;
    
    private Tag tagFromUser1;
    private Tag tagFromUser2;
    
    private Pageable pageable;
    
    @Autowired
    private Flyway flyway;
	
	@BeforeAll
	void setUp(
			@Autowired UserRepository userRepository, 
            @Autowired TagRepository tagRepository, 
            @Autowired TaskRepository taskRepository
            ){
      
		flyway.clean();
        flyway.migrate();
		
        user1 = new User(1L, "Joao", "password", Role.ROLE_USER);
        user2 = new User(2L, "Mario", "password", Role.ROLE_USER);
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        tagFromUser1 = new Tag("STUDY", user1);
        tagFromUser2 = new Tag("HEALTH", user2);
        
        tagRepository.save(tagFromUser1);
        tagRepository.save(tagFromUser2);
        
        taskAFromUser1 = new Task.Builder()
                .id(1L)
                .user(user1)
                .title("Task 1")
                .description("Description 1")
                .status(TaskStatus.UNDONE)
                .priority(TaskPriority.LOW)
                .build();

        taskBFromUser1 = new Task.Builder()
                .id(2L)
                .user(user1)
                .title("Task 2")
                .description("Description 2")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .build();
        
        taskAFromUser2 = new Task.Builder()
                .id(3L)
                .user(user2)
                .title("Task 3")
                .description("Description 3")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH)
                .build();
        
        taskBFromUser2 = new Task.Builder()
                .id(4L)
                .user(user2)
                .title("Task 4")
                .description("Description 4")
                .status(TaskStatus.UNDONE)
                .priority(TaskPriority.HIGH)
                .build();
        
        taskAFromUser1.addTag(tagFromUser1);
        taskBFromUser1.addTag(tagFromUser1);
        
        taskAFromUser2.addTag(tagFromUser2);
        taskBFromUser2.addTag(tagFromUser2);
        
        taskRepository.save(taskAFromUser1);
        taskRepository.save(taskBFromUser1);
        taskRepository.save(taskAFromUser2);
        taskRepository.save(taskBFromUser2);
        
        pageable = PageRequest.of(0, 2);
       
	}
			
	@Test
	void testCustomizedSearchWhenAllParametersAreNull(){
		String status = null;
		String priority = null;
		String tagName = null;
		List<Task> expectedListFromUser1 = Arrays.asList(taskAFromUser1, taskBFromUser1);
		List<Task> expectedListFromUser2 = Arrays.asList(taskAFromUser2, taskBFromUser2);
		
		Page<Task> pageTaskFromUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskFromUser1 = pageTaskFromUser1.getContent();
		
		assertNotNull(pageTaskFromUser1);
		assertThat(expectedListFromUser1).isEqualTo(listTaskFromUser1);
		for (Task task : listTaskFromUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
		}
		
		Page<Task> pageTaskFromUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskFromUser2 = pageTaskFromUser2.getContent();
		
		assertNotNull(pageTaskFromUser2);
		assertThat(pageTaskFromUser2.getTotalElements()).isEqualTo(expectedListFromUser2.size());
		for (Task task : listTaskFromUser2) {
			assertThat(task.getUser().getId()).isEqualTo(user2.getId());
		}
	}
	
	@Test
	void testCustomizedSearchWhenOnlyStatusIsProvided(){
		String status = "DONE";
		String priority = null;
		String tagName = null;
		List<Task> expectedList1 = Arrays.asList(taskBFromUser1);
		List<Task> expectedList2 = Arrays.asList(taskAFromUser2);
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser1 = pageTaskUser1.getContent();
		
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());
		assertThat(listTaskUser1.get(0)).isEqualTo(taskBFromUser1);
		for (Task task : listTaskUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
			assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
		}
		
		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser2 = pageTaskUser2.getContent();
		
		assertNotNull(pageTaskUser2);
		assertThat(pageTaskUser2.getTotalElements()).isEqualTo(expectedList2.size());
		assertThat(listTaskUser2.get(0)).isEqualTo(taskAFromUser2);
		for (Task task : listTaskUser2) {
			assertThat(task.getUser().getId()).isEqualTo(user2.getId());
			assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
		}	
	}
	
	@Test
	void testCustomizedSearchWhenOnlyPriorityIsProvided(){
		String status = null;
		String priority = "MEDIUM";
		String tagName = null;
		List<Task> expectedList1 = Arrays.asList(taskBFromUser1);
		List<Task> expectedList2 = Arrays.asList();
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser1 = pageTaskUser1.getContent();
		
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());
		assertThat(listTaskUser1.get(0)).isEqualTo(taskBFromUser1);
		for (Task task : listTaskUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
			assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
		}
		
		
		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		
		assertThat(pageTaskUser2.getTotalElements()).isEqualTo(expectedList2.size());
		
	}
	
	@Test
	void testCustomizedSearchWhenOnlyTagNameIsProvided(){
		String status = null;
		String priority = null;
		String tagName = tagFromUser1.getName();
		List<Task> expectedList1 = Arrays.asList(taskAFromUser1, taskBFromUser1);
		List<Task> expectedList2 = Arrays.asList();
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser1 = pageTaskUser1.getContent();
		
		assertNotNull(pageTaskUser1);
		assertThat(listTaskUser1.size()).isEqualTo(expectedList1.size());
		for (Task task : listTaskUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
			assertThat(task.getTags().contains(tagFromUser1));
		}
		
		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser2 = pageTaskUser2.getContent();
		
		assertThat(listTaskUser2).isNullOrEmpty();
		assertThat(listTaskUser2.size()).isEqualTo(expectedList2.size());
		for (Task task : listTaskUser2) {
			assertThat(task.getUser().getId()).isEqualTo(user2.getId());
			assertThat(task.getTags().contains(tagFromUser1));
		}
		
	}
	
	@Test
	void testCustomizedSearchWhenOnlyStatusIsNotProvided(){
		String status = null;
		String priority = "HIGH";
		String tagName = tagFromUser2.getName();
		List<Task> expectedList1 = Arrays.asList();
		List<Task> expectedList2 = Arrays.asList(taskAFromUser2, taskBFromUser2);
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());

		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser2 = pageTaskUser2.getContent();
		
		assertNotNull(pageTaskUser2);
		assertThat(listTaskUser2.size()).isEqualTo(expectedList2.size());
		for (Task task : listTaskUser2) {
			assertThat(task.getUser().getId()).isEqualTo(user2.getId());
			assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
			assertThat(task.getTags().contains(tagFromUser2));
		}
	}
	
	@Test
	void testCustomizedSearchWhenOnlyPriorityIsNotProvided(){
		String status = "UNDONE";
		String priority = null;
		String tagName = tagFromUser1.getName();
		List<Task> expectedList1 = Arrays.asList(taskAFromUser1);
		List<Task> expectedList2 = Arrays.asList();
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser1 = pageTaskUser1.getContent();
		
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());
		assertThat(listTaskUser1.get(0)).isEqualTo(taskAFromUser1);
		for (Task task : listTaskUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
			assertThat(task.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(task.getTags().contains(tagFromUser1));
		}

		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser2 = pageTaskUser2.getContent();
		
		assertNotNull(pageTaskUser2);
		assertThat(listTaskUser2.size()).isEqualTo(expectedList2.size());
	}
	
	@Test
	void testCustomizedSearchWhenOnlyTagNameIsNotProvided(){
		String status = "UNDONE";
		String priority = "HIGH";
		String tagName = null;
		List<Task> expectedList1 = Arrays.asList();
		List<Task> expectedList2 = Arrays.asList(taskBFromUser2);
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
	
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());
		

		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser2 = pageTaskUser2.getContent();
		
		assertNotNull(pageTaskUser2);
		assertThat(pageTaskUser2.getTotalElements()).isEqualTo(expectedList2.size());
		assertThat(listTaskUser2.get(0)).isEqualTo(taskBFromUser2);
		for (Task task : listTaskUser2) {
			assertThat(task.getUser().getId()).isEqualTo(user2.getId());
			assertThat(task.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
		}
	}
	
	@Test
	void testCustomizedSearchWhenAllParametersAreProvided(){
		String status = "UNDONE";
		String priority = "LOW";
		String tagName = tagFromUser1.getName();
		List<Task> expectedList1 = Arrays.asList(taskAFromUser1);
		List<Task> expectedList2 = Arrays.asList();
		
		Page<Task> pageTaskUser1 = taskRepository.customizedSearch(user1.getId(), status, priority, tagName, pageable);
		List<Task> listTaskUser1 = pageTaskUser1.getContent();
		
		assertNotNull(pageTaskUser1);
		assertThat(pageTaskUser1.getTotalElements()).isEqualTo(expectedList1.size());
		assertThat(listTaskUser1.get(0)).isEqualTo(taskAFromUser1);
		for (Task task : listTaskUser1) {
			assertThat(task.getUser().getId()).isEqualTo(user1.getId());
			assertThat(task.getStatus()).isEqualTo(TaskStatus.UNDONE);
			assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
			assertThat(task.getTags().contains(tagFromUser1));
		}
		
		Page<Task> pageTaskUser2 = taskRepository.customizedSearch(user2.getId(), status, priority, tagName, pageable);
		
		assertNotNull(pageTaskUser2);
		assertThat(pageTaskUser2.getTotalElements()).isEqualTo(expectedList2.size());
		
	}
}
