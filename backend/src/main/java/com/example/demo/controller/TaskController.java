package com.example.demo.controller;

import com.example.demo.api.TasksApi;
import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.AuthenticationService;
import com.example.demo.service.TaskFilterCriteria;
import com.example.demo.service.TaskPageResult;
import com.example.demo.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskController implements TasksApi {

    private final TaskService taskService;
    private final AuthenticationService authenticationService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            TaskStatus status,
            TaskPriority priority,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Integer page,
            Integer size,
            String sort
    ) {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        TaskPageResult result = taskService.getAllTasks(
                new TaskFilterCriteria(status, priority, dueDateFrom, dueDateTo, page, size, sort),
                user.userId(),
                user.isAdmin()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(result.totalCount()));
        headers.add("X-Total-Pages", String.valueOf(result.totalPages()));
        headers.add("X-Page", String.valueOf(result.page()));
        headers.add("X-Size", String.valueOf(result.size()));
        return ResponseEntity.ok().headers(headers).body(result.items());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> getTaskById(Long id) {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(taskService.getTaskById(id, user.userId(), user.isAdmin()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> createTask(TaskRequest taskRequest) {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskRequest, user.userId()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(Long id, TaskRequest taskRequest) {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(taskService.updateTask(id, taskRequest, user.userId(), user.isAdmin()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTask(Long id) {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        taskService.deleteTask(id, user.userId(), user.isAdmin());
        return ResponseEntity.noContent().build();
    }
}
