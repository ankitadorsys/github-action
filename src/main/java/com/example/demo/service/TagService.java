package com.example.demo.service;

import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TagResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.TagMapper;
import com.example.demo.model.Tag;
import com.example.demo.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .toList();
    }

    public TagResponse getTagById(Long id) {
        return tagMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        Tag tag = Tag.builder()
                .name(request.getName())
                .build();
        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = findOrThrow(id);
        tag.setName(request.getName());
        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", id);
        }
        tagRepository.deleteById(id);
    }

    private Tag findOrThrow(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }
}
