package br.com.dtos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.hateoas.RepresentationModel;

import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import br.com.models.Subtask;
import br.com.models.Tag;

public class TaskResponse extends RepresentationModel<TaskResponse> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String title;
	private String description;
	private TaskStatus status;
	private TaskPriority priority;
	private LocalDate createdDate;
	private LocalDate dueDate;
	private Set<Tag> tags = new HashSet<>();
	private List<Subtask> subtasks = new ArrayList<>();
	
	public TaskResponse() {
	}

	public TaskResponse(Long id, String title, String description, TaskStatus status, TaskPriority priority,
			LocalDate createdDate, LocalDate dueDate) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.status = status;
		this.priority = priority;
		this.createdDate = createdDate;
		this.dueDate = dueDate;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Set<Tag> getTags() {
		return tags;
	}
	
	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(Tag tag) {
		tags.add(tag);
	}
	
	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	public List<Subtask> getSubtasks() {
		return subtasks;
	}

	public void setSubtasks(List<Subtask> subtasks) {
		this.subtasks = subtasks;
	}

	public void addSbubtask(Subtask subtask) {
		subtasks.add(subtask);
	}
	
	public void removeSubtask(Subtask subtask) {
		subtasks.remove(subtask);
	}

	@Override
	public String toString() {
		return "TaskResponse [id=" + id + ", title=" + title + ", description=" + description + ", status=" + status
				+ ", priority=" + priority + ", createdDate=" + createdDate + ", dueDate=" + dueDate + "]";
	}
	
	
	
}