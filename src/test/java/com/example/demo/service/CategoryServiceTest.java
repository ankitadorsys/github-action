package com.example.demo.service;

import com.example.demo.api.model.CategoryRequest;
import com.example.demo.api.model.CategoryResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory() {
        return Category.builder()
                .id(1L)
                .name("Work")
                .color("#FF0000")
                .createdAt(LocalDateTime.now())
                .tasks(new ArrayList<>())
                .build();
    }

    private CategoryResponse sampleResponse() {
        return new CategoryResponse()
                .id(1L)
                .name("Work")
                .color("#FF0000")
                .taskCount(0);
    }

    private CategoryRequest sampleRequest() {
        return new CategoryRequest("Work").color("#FF0000");
    }

    @Test
    @DisplayName("getAllCategories - returns mapped list of category responses")
    void getAllCategories_returnsMappedList() {
        Category category = sampleCategory();
        given(categoryRepository.findAll()).willReturn(List.of(category));
        given(categoryMapper.toResponse(category)).willReturn(sampleResponse());

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Work");
    }

    @Test
    @DisplayName("getCategoryById - returns category when found")
    void getCategoryById_returnsCategory() {
        Category category = sampleCategory();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryMapper.toResponse(category)).willReturn(sampleResponse());

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("Work");
    }

    @Test
    @DisplayName("getCategoryById - throws ResourceNotFoundException when not found")
    void getCategoryById_throwsWhenNotFound() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    @DisplayName("createCategory - saves and returns mapped response")
    void createCategory_savesAndReturns() {
        Category saved = sampleCategory();
        given(categoryRepository.save(any(Category.class))).willReturn(saved);
        given(categoryMapper.toResponse(saved)).willReturn(sampleResponse());

        CategoryResponse result = categoryService.createCategory(sampleRequest());

        assertThat(result.getName()).isEqualTo("Work");
        then(categoryRepository).should().save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - updates fields and returns mapped response")
    void updateCategory_updatesAndReturns() {
        Category existing = sampleCategory();
        CategoryResponse updated = sampleResponse().name("Personal");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.save(any(Category.class))).willReturn(existing);
        given(categoryMapper.toResponse(existing)).willReturn(updated);

        CategoryRequest request = new CategoryRequest("Personal").color("#00FF00");
        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertThat(result.getName()).isEqualTo("Personal");
    }

    @Test
    @DisplayName("deleteCategory - deletes when category exists")
    void deleteCategory_deletesWhenExists() {
        given(categoryRepository.existsById(1L)).willReturn(true);

        categoryService.deleteCategory(1L);

        then(categoryRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteCategory - throws ResourceNotFoundException when not found")
    void deleteCategory_throwsWhenNotFound() {
        given(categoryRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        then(categoryRepository).should(never()).deleteById(99L);
    }
}
