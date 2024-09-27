package br.com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.models.Subtask;

public interface SubtaskRepository extends JpaRepository<Subtask, Long>{
}
