package com.example.demo.mapper;

import com.example.demo.api.model.TaskResponse;
import com.example.demo.model.Task;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(uses = {CategoryMapper.class, TagMapper.class, DateTimeMapper.class})
public interface TaskMapper {

    @Mapping(target = "commentCount", source = "task", qualifiedByName = "commentCount")
    TaskResponse toResponse(Task task);

    @Named("commentCount")
    default int commentCount(Task task) {
        List<?> comments = task.getComments();
        return comments != null ? comments.size() : 0;
    }
}
