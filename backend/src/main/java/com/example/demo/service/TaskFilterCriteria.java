package com.example.demo.service;

import com.example.demo.api.model.TaskPriority;
import com.example.demo.api.model.TaskStatus;
import java.time.LocalDate;

public record TaskFilterCriteria(
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        Integer page,
        Integer size,
        String sort
) {
}
