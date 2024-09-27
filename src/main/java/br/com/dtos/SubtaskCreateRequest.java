package br.com.dtos;

import java.io.Serializable;
import java.time.LocalDate;

import br.com.enums.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubtaskCreateRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@NotBlank
	private String title;
	@Size(max = 500, message = "Description cannot exceed 500 characters")
	private String description;
	@Future(message = "The date must be in the future!")
	private LocalDate dueDate;
	private TaskStatus status;
	
	public SubtaskCreateRequest() {
	}

	public SubtaskCreateRequest(@NotBlank String title,
			@Size(max = 500, message = "Description cannot exceed 500 characters") String description,
			@Future(message = "The date must be in the future!") LocalDate dueDate, TaskStatus status) {
		this.title = title;
		this.description = description;
		this.dueDate = dueDate;
		this.status = status;
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

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}	
}
