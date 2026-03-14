package com.example.demo.mapper;

import com.example.demo.api.model.CategoryResponse;
import com.example.demo.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateTimeMapper.class})
public interface CategoryMapper {

    @Mapping(target = "taskCount", expression = "java(category.getTasks() != null ? category.getTasks().size() : 0)")
    CategoryResponse toResponse(Category category);
}
