package com.example.demo.repository;

import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.model.Task;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        if (priority == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> dueDateOnOrAfter(LocalDate dueDateFrom) {
        if (dueDateFrom == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom);
    }

    public static Specification<Task> dueDateOnOrBefore(LocalDate dueDateTo) {
        if (dueDateTo == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), dueDateTo);
    }

    public static Specification<Task> hasUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("userId"), userId);
    }
}
