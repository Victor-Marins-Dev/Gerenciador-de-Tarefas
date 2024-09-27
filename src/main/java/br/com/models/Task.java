package br.com.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.enums.TaskPriority;
import br.com.enums.TaskStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	private String title;
	private String description;
	@Enumerated(EnumType.STRING)
	private TaskStatus status;
	@Enumerated(EnumType.STRING)
	private TaskPriority priority;
	private LocalDate createdDate;
	private LocalDate dueDate;
	
	@ManyToMany
	@JoinTable(name = "task_tags",
			joinColumns = @JoinColumn(name = "task_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<Tag> tags = new HashSet<>();
	
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Subtask> subtasks = new ArrayList<>();
	
	public Task() {
	}

	public Task(User user, String title) {
		this.user = user;
		this.title = title;
	}

	public Task(User user, String title, String description, TaskPriority priority,
			LocalDate dueDate) {
		this.user = user;
		this.title = title;
		this.description = description;
		this.priority = priority;
		this.dueDate = dueDate;
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
	
	public void addTag(Tag tag) {
		tags.add(tag);
	}
	
	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	public List<Subtask> getSubtasks() {
		return subtasks;
	}

	public void addSubtask(Subtask subtask) {
		subtasks.add(subtask);
	}
	
	public void removeSubtasks(Subtask subtask) {
		subtasks.remove(subtask);
	}
	
	@Override
	public String toString() {
		return "Task [id=" + id + ", user=" + user + ", title=" + title + ", description=" + description + ", status="
				+ status + ", priority=" + priority + ", createdDate=" + createdDate + ", dueDate=" + dueDate + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Task other = (Task) obj;
		return Objects.equals(id, other.id);
	}
	
	private Task(Builder builder) {
		this.id = builder.id;
		this.user = builder.user;
		this.title = builder.title;
		this.description = builder.description;
		this.status = builder.status;
		this.priority = builder.priority;
		this.createdDate = builder.createdDate;
		this.dueDate = builder.dueDate;
	}
	
	public static class Builder {
		private Long id;
		private User user;
		private String title;
		private String description;
		private TaskStatus status;
		private TaskPriority priority;
		private LocalDate createdDate;
		private LocalDate dueDate;
		
		public Builder id(Long id) {
			this.id = id;
			return this;
		}
		
		public Builder user(User user) {
			this.user = user;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder status(TaskStatus status) {
			this.status = status;
			return this;
		}
		
		public Builder priority(TaskPriority priority) {
			this.priority = priority;
			return this;
		}
		
		public Builder createdDate(LocalDate createdDate) {
			this.createdDate = createdDate;
			return this;
		}
		
		public Builder dueDate(LocalDate dueDate) {
			this.dueDate = dueDate;
			return this;
		}
		
		public Task build() {
			return new Task(this);
		}
	}
}
