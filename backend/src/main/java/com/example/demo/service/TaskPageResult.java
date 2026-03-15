package com.example.demo.service;

import com.example.demo.api.model.TaskResponse;
import java.util.List;

public record TaskPageResult(
        List<TaskResponse> items,
        long totalCount,
        int totalPages,
        int page,
        int size
) {
}
