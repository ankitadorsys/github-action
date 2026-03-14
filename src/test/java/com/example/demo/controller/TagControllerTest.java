package com.example.demo.controller;

import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TagResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.TagService;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TagController.class, GlobalExceptionHandler.class})
@DisplayName("TagController")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    private TagResponse sampleResponse() {
        return new TagResponse().id(1L).name("urgent");
    }

    private TagRequest sampleRequest() {
        return new TagRequest("urgent");
    }

    @Test
    @DisplayName("GET /api/tags - returns list of tags")
    void getAllTags_returnsList() throws Exception {
        given(tagService.getAllTags()).willReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("urgent"));
    }

    @Test
    @DisplayName("GET /api/tags/{id} - returns tag by id")
    void getTagById_returnsTag() throws Exception {
        given(tagService.getTagById(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("urgent"));
    }

    @Test
    @DisplayName("GET /api/tags/{id} - returns 404 when tag not found")
    void getTagById_notFound_returns404() throws Exception {
        given(tagService.getTagById(99L))
                .willThrow(new ResourceNotFoundException("Tag", 99L));

        mockMvc.perform(get("/api/tags/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tags - creates and returns new tag")
    void createTag_returnsCreated() throws Exception {
        given(tagService.createTag(any(TagRequest.class))).willReturn(sampleResponse());

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("urgent"));
    }

    @Test
    @DisplayName("PUT /api/tags/{id} - updates and returns tag")
    void updateTag_returnsUpdated() throws Exception {
        TagResponse updated = new TagResponse().id(1L).name("important");
        given(tagService.updateTag(eq(1L), any(TagRequest.class))).willReturn(updated);

        TagRequest request = new TagRequest("important");
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("important"));
    }

    @Test
    @DisplayName("DELETE /api/tags/{id} - returns 204 no content")
    void deleteTag_returnsNoContent() throws Exception {
        willDoNothing().given(tagService).deleteTag(1L);

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tags/{id} - returns 404 when tag not found")
    void deleteTag_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Tag", 99L))
                .given(tagService).deleteTag(99L);

        mockMvc.perform(delete("/api/tags/99"))
                .andExpect(status().isNotFound());
    }
}
