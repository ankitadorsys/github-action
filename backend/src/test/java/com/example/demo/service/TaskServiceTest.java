package com.example.demo.service;

import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.model.Category;
import com.example.demo.model.Tag;
import com.example.demo.model.Task;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
class TaskServiceTest {

    private static final String USER_ID = "user1-uuid";
    private static final String OTHER_USER_ID = "other-uuid";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask() {
        return Task.builder()
                .id(1L)
                .title("Test Task")
                .description("A test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 4, 1))
                .userId(USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .tags(new HashSet<>())
                .comments(new ArrayList<>())
                .build();
    }

    private TaskResponse sampleResponse() {
        return new TaskResponse()
                .id(1L)
                .title("Test Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .userId(USER_ID)
                .commentCount(0);
    }

    private TaskRequest sampleRequest() {
        return new TaskRequest("Test Task")
                .description("A test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("getAllTasks - regular user gets only their own tasks")
    void getAllTasks_userGetsOwnTasks() {
        Task task = sampleTask();
        TaskResponse response = sampleResponse();
        given(taskRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(new PageImpl<>(List.of(task)));
        given(taskMapper.toResponse(task)).willReturn(response);

        List<TaskResponse> result = taskService.getAllTasks(USER_ID, false);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test Task");
        then(taskRepository).should().findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllTasks - admin gets all tasks")
    void getAllTasks_adminGetsAllTasks() {
        Task task = sampleTask();
        TaskResponse response = sampleResponse();
        given(taskRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(new PageImpl<>(List.of(task)));
        given(taskMapper.toResponse(task)).willReturn(response);

        List<TaskResponse> result = taskService.getAllTasks(USER_ID, true);

        assertThat(result).hasSize(1);
        then(taskRepository).should().findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getTaskById - owner can access their own task")
    void getTaskById_ownerCanAccess() {
        Task task = sampleTask();
        TaskResponse response = sampleResponse();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskMapper.toResponse(task)).willReturn(response);

        TaskResponse result = taskService.getTaskById(1L, USER_ID, false);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTaskById - admin can access any task")
    void getTaskById_adminCanAccessAnyTask() {
        Task task = sampleTask();
        TaskResponse response = sampleResponse();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskMapper.toResponse(task)).willReturn(response);

        TaskResponse result = taskService.getTaskById(1L, OTHER_USER_ID, true);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTaskById - non-owner gets AccessDeniedException")
    void getTaskById_nonOwnerGetsDenied() {
        Task task = sampleTask();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getTaskById(1L, OTHER_USER_ID, false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("getTaskById - throws TaskNotFoundException when not found")
    void getTaskById_throwsWhenNotFound() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L, USER_ID, false))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("createTask - saves task with userId and returns mapped response")
    void createTask_savesWithUserIdAndReturns() {
        Task savedTask = sampleTask();
        TaskResponse response = sampleResponse();
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);
        given(taskMapper.toResponse(savedTask)).willReturn(response);

        TaskResponse result = taskService.createTask(sampleRequest(), USER_ID);

        assertThat(result.getTitle()).isEqualTo("Test Task");
        then(taskRepository).should().save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - resolves category when categoryId is provided")
    void createTask_resolvesCategoryWhenProvided() {
        TaskRequest request = sampleRequest().categoryId(1L);
        Category category = Category.builder().id(1L).name("Work").build();
        Task savedTask = sampleTask();
        savedTask.setCategory(category);

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);
        given(taskMapper.toResponse(savedTask)).willReturn(sampleResponse());

        taskService.createTask(request, USER_ID);

        then(categoryRepository).should().findById(1L);
    }

    @Test
    @DisplayName("createTask - resolves tags when tagIds are provided")
    void createTask_resolvesTagsWhenProvided() {
        Tag tag = Tag.builder().id(1L).name("urgent").build();
        TaskRequest request = sampleRequest().tagIds(Set.of(1L));
        Task savedTask = sampleTask();

        given(tagRepository.findAllById(Set.of(1L))).willReturn(List.of(tag));
        given(taskRepository.save(any(Task.class))).willReturn(savedTask);
        given(taskMapper.toResponse(savedTask)).willReturn(sampleResponse());

        taskService.createTask(request, USER_ID);

        then(tagRepository).should().findAllById(Set.of(1L));
    }

    @Test
    @DisplayName("updateTask - owner can update their own task")
    void updateTask_ownerCanUpdate() {
        Task existing = sampleTask();
        TaskResponse response = sampleResponse().title("Updated");
        given(taskRepository.findById(1L)).willReturn(Optional.of(existing));
        given(taskRepository.save(any(Task.class))).willReturn(existing);
        given(taskMapper.toResponse(existing)).willReturn(response);

        TaskRequest request = sampleRequest().title("Updated");
        TaskResponse result = taskService.updateTask(1L, request, USER_ID, false);

        assertThat(result.getTitle()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("updateTask - non-owner gets AccessDeniedException")
    void updateTask_nonOwnerGetsDenied() {
        Task existing = sampleTask();
        given(taskRepository.findById(1L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.updateTask(1L, sampleRequest(), OTHER_USER_ID, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("updateTask - throws TaskNotFoundException when not found")
    void updateTask_throwsWhenNotFound() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, sampleRequest(), USER_ID, false))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("deleteTask - owner can delete their own task")
    void deleteTask_ownerCanDelete() {
        Task task = sampleTask();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        taskService.deleteTask(1L, USER_ID, false);

        then(taskRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask - admin can delete any task")
    void deleteTask_adminCanDeleteAnyTask() {
        Task task = sampleTask();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        taskService.deleteTask(1L, OTHER_USER_ID, true);

        then(taskRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask - non-owner gets AccessDeniedException")
    void deleteTask_nonOwnerGetsDenied() {
        Task task = sampleTask();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.deleteTask(1L, OTHER_USER_ID, false))
                .isInstanceOf(AccessDeniedException.class);
        then(taskRepository).should(never()).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask - throws TaskNotFoundException when not found")
    void deleteTask_throwsWhenNotFound() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L, USER_ID, false))
                .isInstanceOf(TaskNotFoundException.class);
        then(taskRepository).should(never()).deleteById(99L);
    }
}
