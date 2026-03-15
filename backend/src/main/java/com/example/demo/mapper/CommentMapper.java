package com.example.demo.mapper;

import com.example.demo.api.model.CommentResponse;
import com.example.demo.model.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateTimeMapper.class})
public interface CommentMapper {

    @Mapping(source = "task.id", target = "taskId")
    CommentResponse toResponse(TaskComment comment);
}
