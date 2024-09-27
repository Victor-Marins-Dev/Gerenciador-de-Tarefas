package br.com.dtos;

import java.io.Serializable;

import org.springframework.hateoas.RepresentationModel;

import jakarta.validation.constraints.NotBlank;

public class TagDto extends RepresentationModel<TagDto> implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Long id;
	@NotBlank(message = "Tag name can not be blank")
	private String name;
	
	public TagDto() {
	}

	public TagDto(String name) {
		this.name = name;
	}
	
	public TagDto(Long id, @NotBlank(message = "Tag name can not be blank") String tagName) {
		this.id = id;
		this.name = tagName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
