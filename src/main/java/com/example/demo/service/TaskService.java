package com.example.demo.service;

import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.api.model.TaskPriority;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return taskMapper.toResponse(findTaskOrThrow(id));
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
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
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);

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
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
