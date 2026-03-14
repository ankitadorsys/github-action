package com.example.demo.controller;

import com.example.demo.api.TagsApi;
import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TagResponse;
import com.example.demo.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController implements TagsApi {

    private final TagService tagService;

    @Override
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @Override
    public ResponseEntity<TagResponse> getTagById(Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @Override
    public ResponseEntity<TagResponse> createTag(TagRequest tagRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tagRequest));
    }

    @Override
    public ResponseEntity<TagResponse> updateTag(Long id, TagRequest tagRequest) {
        return ResponseEntity.ok(tagService.updateTag(id, tagRequest));
    }

    @Override
    public ResponseEntity<Void> deleteTag(Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
