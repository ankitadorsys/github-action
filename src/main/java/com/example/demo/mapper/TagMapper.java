package com.example.demo.mapper;

import com.example.demo.api.model.TagResponse;
import com.example.demo.model.Tag;
import org.mapstruct.Mapper;

@Mapper
public interface TagMapper {

    TagResponse toResponse(Tag tag);
}
