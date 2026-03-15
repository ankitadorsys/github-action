package com.example.demo.controller;

import com.example.demo.api.model.CategoryResponse;
import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.config.TestSecurityConfig;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.AuthenticationService;
import com.example.demo.service.TaskFilterCriteria;
import com.example.demo.service.TaskPageResult;
import com.example.demo.service.TaskService;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TaskController.class, GlobalExceptionHandler.class})
@Import(TestSecurityConfig.class)
@DisplayName("TaskController")
class TaskControllerTest {

    private static final String USER_ID = "user1-uuid";
    private static final AuthenticatedUser USER = new AuthenticatedUser(USER_ID, "user1", Set.of("ROLE_USER"));
    private static final AuthenticatedUser ADMIN = new AuthenticatedUser("admin-uuid", "admin", Set.of("ROLE_USER", "ROLE_ADMIN"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private AuthenticationService authenticationService;

    private TaskResponse sampleResponse() {
        return new TaskResponse()
                .id(1L)
                .title("Test Task")
                .description("A test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 4, 1))
                .userId(USER_ID)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .category(new CategoryResponse().id(1L).name("Work").color("#FF0000"))
                .tags(Set.of())
                .commentCount(0);
    }

    private TaskRequest sampleRequest() {
        return new TaskRequest("Test Task")
                .description("A test task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.of(2026, 4, 1))
                .categoryId(1L)
                .tagIds(Set.of());
    }

    @Test
    @DisplayName("GET /api/tasks - returns list of tasks")
    void getAllTasks_returnsListOfTasks() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.getAllTasks(any(TaskFilterCriteria.class), eq(USER_ID), eq(false)))
                .willReturn(new TaskPageResult(List.of(sampleResponse()), 1, 1, 0, 10));

        mockMvc.perform(get("/api/tasks")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    @DisplayName("GET /api/tasks - returns empty list when no tasks exist")
    void getAllTasks_returnsEmptyList() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.getAllTasks(any(TaskFilterCriteria.class), eq(USER_ID), eq(false)))
                .willReturn(new TaskPageResult(List.of(), 0, 0, 0, 10));

        mockMvc.perform(get("/api/tasks")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - returns task by id")
    void getTaskById_returnsTask() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.getTaskById(1L, USER_ID, false)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/tasks/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - returns 404 when task not found")
    void getTaskById_notFound_returns404() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.getTaskById(99L, USER_ID, false)).willThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/tasks - creates and returns new task")
    void createTask_returnsCreatedTask() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.createTask(any(TaskRequest.class), eq(USER_ID))).willReturn(sampleResponse());

        mockMvc.perform(post("/api/tasks")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - updates and returns task")
    void updateTask_returnsUpdatedTask() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        TaskResponse updated = sampleResponse().title("Updated Task");
        given(taskService.updateTask(eq(1L), any(TaskRequest.class), eq(USER_ID), eq(false))).willReturn(updated);

        TaskRequest request = sampleRequest().title("Updated Task");
        mockMvc.perform(put("/api/tasks/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - returns 404 when task not found")
    void updateTask_notFound_returns404() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        given(taskService.updateTask(eq(99L), any(TaskRequest.class), eq(USER_ID), eq(false)))
                .willThrow(new TaskNotFoundException(99L));

        mockMvc.perform(put("/api/tasks/99")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - returns 204 no content")
    void deleteTask_returnsNoContent() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        willDoNothing().given(taskService).deleteTask(1L, USER_ID, false);

        mockMvc.perform(delete("/api/tasks/1")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - returns 404 when task not found")
    void deleteTask_notFound_returns404() throws Exception {
        given(authenticationService.getCurrentUser()).willReturn(USER);
        willThrow(new TaskNotFoundException(99L)).given(taskService).deleteTask(99L, USER_ID, false);

        mockMvc.perform(delete("/api/tasks/99")
                        .with(jwt().jwt(j -> j.subject(USER_ID).claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isNotFound());
    }
}
