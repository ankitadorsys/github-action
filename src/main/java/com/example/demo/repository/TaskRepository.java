package com.example.demo.repository;

import com.example.demo.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(String userId);

    Optional<Task> findByIdAndUserId(Long id, String userId);

    boolean existsByIdAndUserId(Long id, String userId);
}
