package br.com.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.models.Task;

public interface TaskRepository extends JpaRepository<Task, Long>{
	
	public Page<Task> findAllByUserId(Long userId, Pageable pageable);
	

	@Query(nativeQuery = true,
	           value = "SELECT " +
	                   "    t.id, " +
	                   "    t.title, " +
	                   "    t.description, " +
	                   "    t.status, " +
	                   "    t.priority, " +
	                   "    t.created_date, " +
	                   "    t.due_date, " +
	                   "    t.user_id, " +
	                   "    tg.name " +
	                   "FROM tasks t " +
	                   "LEFT JOIN task_tags tt ON t.id = tt.task_id " +
	                   "LEFT JOIN tags tg ON tt.tag_id = tg.id " +
	                   "WHERE t.user_id = :userId " +
	                   "AND (:statusFromSearch IS NULL OR t.status = :statusFromSearch) " +
	                   "AND (:priorityFromSearch IS NULL OR t.priority = :priorityFromSearch) " +
	                   "AND (:tagNameFromSearch IS NULL OR tg.name = :tagNameFromSearch)" +
	                   "")
	public Page<Task> customizedSearch(
			@Param("userId") Long userId,
			@Param("statusFromSearch") String status, 
			@Param("priorityFromSearch") String priority, 
			@Param("tagNameFromSearch") String tagName,
			Pageable pageable
			);
}
