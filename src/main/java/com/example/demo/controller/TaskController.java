package com.example.demo.controller;

import com.example.demo.api.TasksApi;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskResponse;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.AuthenticationService;
import com.example.demo.service.TaskService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        AuthenticatedUser user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(taskService.getAllTasks(user.userId(), user.isAdmin()));
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
