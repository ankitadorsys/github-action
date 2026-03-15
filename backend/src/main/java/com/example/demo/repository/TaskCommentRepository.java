package com.example.demo.repository;

import com.example.demo.model.TaskComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskId(Long taskId);
}
