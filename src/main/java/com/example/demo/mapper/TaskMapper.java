package com.example.demo.mapper;

import com.example.demo.api.model.TaskResponse;
import com.example.demo.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {CategoryMapper.class, TagMapper.class, DateTimeMapper.class})
public interface TaskMapper {

    @Mapping(target = "commentCount", expression = "java(task.getComments().size())")
    TaskResponse toResponse(Task task);
}
