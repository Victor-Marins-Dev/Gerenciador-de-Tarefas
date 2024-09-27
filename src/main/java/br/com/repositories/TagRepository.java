package br.com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.models.Tag;

public interface TagRepository extends JpaRepository<Tag, Long>{
	
	public List<Tag> findAllByUserId(Long id);
	
}
