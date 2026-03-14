package com.example.demo.mapper;

import com.example.demo.api.model.CategoryResponse;
import com.example.demo.model.Category;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(uses = {DateTimeMapper.class})
public interface CategoryMapper {

    @Mapping(target = "taskCount", source = "category", qualifiedByName = "taskCount")
    CategoryResponse toResponse(Category category);

    @Named("taskCount")
    default int taskCount(Category category) {
        List<?> tasks = category.getTasks();
        return tasks != null ? tasks.size() : 0;
    }
}
