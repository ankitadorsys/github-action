package com.example.demo.controller;

import com.example.demo.api.TagsApi;
import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TagResponse;
import com.example.demo.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController implements TagsApi {

    private final TagService tagService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TagResponse> getTagById(Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TagResponse> createTag(TagRequest tagRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tagRequest));
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TagResponse> updateTag(Long id, TagRequest tagRequest) {
        return ResponseEntity.ok(tagService.updateTag(id, tagRequest));
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTag(Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
