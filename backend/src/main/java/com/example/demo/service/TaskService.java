package com.example.demo.service;

import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.model.Category;
import com.example.demo.model.Tag;
import com.example.demo.model.Task;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.TaskSpecifications;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;

    public List<TaskResponse> getAllTasks(String userId, boolean isAdmin) {
        TaskPageResult result = getAllTasks(
                new TaskFilterCriteria(null, null, null, null, 0, 100, "updatedAt,desc"),
                userId,
                isAdmin
        );
        return result.items();
    }

    public TaskPageResult getAllTasks(TaskFilterCriteria criteria, String userId, boolean isAdmin) {
        Pageable pageable = toPageable(criteria.page(), criteria.size(), criteria.sort());

        Specification<Task> specification = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());
        specification = andIfPresent(specification, TaskSpecifications.hasStatus(criteria.status()));
        specification = andIfPresent(specification, TaskSpecifications.hasPriority(criteria.priority()));
        specification = andIfPresent(specification, TaskSpecifications.dueDateOnOrAfter(criteria.dueDateFrom()));
        specification = andIfPresent(specification, TaskSpecifications.dueDateOnOrBefore(criteria.dueDateTo()));

        if (!isAdmin) {
            specification = andIfPresent(specification, TaskSpecifications.hasUserId(userId));
        }

        Page<Task> taskPage = taskRepository.findAll(specification, pageable);

        return new TaskPageResult(
                taskPage.getContent().stream().map(taskMapper::toResponse).toList(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.getNumber(),
                taskPage.getSize()
        );
    }

    public TaskResponse getTaskById(Long id, String userId, boolean isAdmin) {
        Task task = findTaskOrThrow(id);
        verifyOwnership(task, userId, isAdmin);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, String userId) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .userId(userId)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            task.setCategory(category);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            task.setTags(tags);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, String userId, boolean isAdmin) {
        Task task = findTaskOrThrow(id);
        verifyOwnership(task, userId, isAdmin);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        task.setDueDate(request.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }

        if (request.getTagIds() != null) {
            task.getTags().clear();
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            task.setTags(tags);
        }

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id, String userId, boolean isAdmin) {
        Task task = findTaskOrThrow(id);
        verifyOwnership(task, userId, isAdmin);
        taskRepository.deleteById(id);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void verifyOwnership(Task task, String userId, boolean isAdmin) {
        if (!isAdmin && !userId.equals(task.getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this task");
        }
    }

    private Pageable toPageable(Integer page, Integer size, String sortExpression) {
        int pageValue = page != null && page >= 0 ? page : 0;
        int sizeValue = size != null && size > 0 && size <= 100 ? size : 10;

        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));
        if (sortExpression != null && !sortExpression.isBlank()) {
            String[] parts = sortExpression.split(",");
            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";

            if (field.equals("title") || field.equals("dueDate") || field.equals("priority")
                    || field.equals("status") || field.equals("createdAt") || field.equals("updatedAt")) {
                Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sort = Sort.by(new Sort.Order(sortDirection, field));
            }
        }

        return PageRequest.of(pageValue, sizeValue, sort);
    }

    private Specification<Task> andIfPresent(Specification<Task> base, Specification<Task> optional) {
        if (optional == null) {
            return base;
        }
        return base.and(optional);
    }
}
