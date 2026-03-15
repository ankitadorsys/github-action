package com.example.demo.service;

import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TagResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.TagMapper;
import com.example.demo.model.Tag;
import com.example.demo.repository.TagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private Tag sampleTag() {
        return Tag.builder()
                .id(1L)
                .name("urgent")
                .tasks(new HashSet<>())
                .build();
    }

    private TagResponse sampleResponse() {
        return new TagResponse().id(1L).name("urgent");
    }

    private TagRequest sampleRequest() {
        return new TagRequest("urgent");
    }

    @Test
    @DisplayName("getAllTags - returns mapped list of tag responses")
    void getAllTags_returnsMappedList() {
        Tag tag = sampleTag();
        given(tagRepository.findAll()).willReturn(List.of(tag));
        given(tagMapper.toResponse(tag)).willReturn(sampleResponse());

        List<TagResponse> result = tagService.getAllTags();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("urgent");
    }

    @Test
    @DisplayName("getTagById - returns tag when found")
    void getTagById_returnsTag() {
        Tag tag = sampleTag();
        given(tagRepository.findById(1L)).willReturn(Optional.of(tag));
        given(tagMapper.toResponse(tag)).willReturn(sampleResponse());

        TagResponse result = tagService.getTagById(1L);

        assertThat(result.getName()).isEqualTo("urgent");
    }

    @Test
    @DisplayName("getTagById - throws ResourceNotFoundException when not found")
    void getTagById_throwsWhenNotFound() {
        given(tagRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag");
    }

    @Test
    @DisplayName("createTag - saves and returns mapped response")
    void createTag_savesAndReturns() {
        Tag saved = sampleTag();
        given(tagRepository.save(any(Tag.class))).willReturn(saved);
        given(tagMapper.toResponse(saved)).willReturn(sampleResponse());

        TagResponse result = tagService.createTag(sampleRequest());

        assertThat(result.getName()).isEqualTo("urgent");
        then(tagRepository).should().save(any(Tag.class));
    }

    @Test
    @DisplayName("updateTag - updates name and returns mapped response")
    void updateTag_updatesAndReturns() {
        Tag existing = sampleTag();
        TagResponse updated = new TagResponse().id(1L).name("important");
        given(tagRepository.findById(1L)).willReturn(Optional.of(existing));
        given(tagRepository.save(any(Tag.class))).willReturn(existing);
        given(tagMapper.toResponse(existing)).willReturn(updated);

        TagRequest request = new TagRequest("important");
        TagResponse result = tagService.updateTag(1L, request);

        assertThat(result.getName()).isEqualTo("important");
    }

    @Test
    @DisplayName("deleteTag - deletes when tag exists")
    void deleteTag_deletesWhenExists() {
        given(tagRepository.existsById(1L)).willReturn(true);

        tagService.deleteTag(1L);

        then(tagRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteTag - throws ResourceNotFoundException when not found")
    void deleteTag_throwsWhenNotFound() {
        given(tagRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> tagService.deleteTag(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        then(tagRepository).should(never()).deleteById(99L);
    }
}
