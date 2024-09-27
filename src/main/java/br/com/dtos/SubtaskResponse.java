package br.com.dtos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.hateoas.RepresentationModel;

import br.com.enums.TaskStatus;

public class SubtaskResponse extends RepresentationModel<SubtaskResponse> implements Serializable { 
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String title;
	private String description;
	private LocalDate createdDate;
	private LocalDate dueDate;
	private TaskStatus status;
	
	public SubtaskResponse() {
	}

	public SubtaskResponse(Long id, String title, String description, LocalDate createdDate, LocalDate dueDate,
			TaskStatus status) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.createdDate = createdDate;
		this.dueDate = dueDate;
		this.status = status;
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

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
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
		SubtaskResponse other = (SubtaskResponse) obj;
		return Objects.equals(id, other.id);
	}
}
