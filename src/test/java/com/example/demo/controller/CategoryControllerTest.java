package com.example.demo.controller;

import com.example.demo.api.model.CategoryRequest;
import com.example.demo.api.model.CategoryResponse;
import com.example.demo.config.TestSecurityConfig;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.CategoryService;
import tools.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CategoryController.class, GlobalExceptionHandler.class})
@Import(TestSecurityConfig.class)
@DisplayName("CategoryController")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryResponse sampleResponse() {
        return new CategoryResponse()
                .id(1L)
                .name("Work")
                .color("#FF0000")
                .createdAt(OffsetDateTime.now())
                .taskCount(3);
    }

    private CategoryRequest sampleRequest() {
        return new CategoryRequest("Work").color("#FF0000");
    }

    @Test
    @DisplayName("GET /api/categories - returns list of categories as USER")
    void getAllCategories_returnsList() throws Exception {
        given(categoryService.getAllCategories()).willReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/categories")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Work"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - returns category by id as USER")
    void getCategoryById_returnsCategory() throws Exception {
        given(categoryService.getCategoryById(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/categories/1")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.color").value("#FF0000"))
                .andExpect(jsonPath("$.taskCount").value(3));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - returns 404 when category not found")
    void getCategoryById_notFound_returns404() throws Exception {
        given(categoryService.getCategoryById(99L))
                .willThrow(new ResourceNotFoundException("Category", 99L));

        mockMvc.perform(get("/api/categories/99")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_USER"))))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/categories - creates and returns new category as ADMIN")
    void createCategory_returnsCreated() throws Exception {
        given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(sampleResponse());

        mockMvc.perform(post("/api/categories")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} - updates and returns category as ADMIN")
    void updateCategory_returnsUpdated() throws Exception {
        CategoryResponse updated = sampleResponse().name("Personal");
        given(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).willReturn(updated);

        CategoryRequest request = new CategoryRequest("Personal").color("#00FF00");
        mockMvc.perform(put("/api/categories/1")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN")))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Personal"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - returns 204 no content as ADMIN")
    void deleteCategory_returnsNoContent() throws Exception {
        willDoNothing().given(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - returns 404 when category not found")
    void deleteCategory_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Category", 99L))
                .given(categoryService).deleteCategory(99L);

        mockMvc.perform(delete("/api/categories/99")
                        .with(jwt().jwt(j -> j.claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN"))))))
                .andExpect(status().isNotFound());
    }
}
