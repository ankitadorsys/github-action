package com.example.demo.controller;

import com.example.demo.api.model.CommentRequest;
import com.example.demo.api.model.CommentResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.service.CommentService;
import tools.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
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

@WebMvcTest({CommentController.class, GlobalExceptionHandler.class})
@DisplayName("CommentController")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private CommentResponse sampleResponse() {
        return new CommentResponse()
                .id(1L)
                .content("Great progress!")
                .authorName("Ankit")
                .createdAt(OffsetDateTime.now())
                .taskId(1L);
    }

    private CommentRequest sampleRequest() {
        return new CommentRequest("Great progress!", "Ankit");
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/comments - returns list of comments")
    void getCommentsByTaskId_returnsList() throws Exception {
        given(commentService.getCommentsByTaskId(1L)).willReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/tasks/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("Great progress!"))
                .andExpect(jsonPath("$[0].authorName").value("Ankit"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskId}/comments - returns 404 when task not found")
    void getCommentsByTaskId_taskNotFound_returns404() throws Exception {
        given(commentService.getCommentsByTaskId(99L))
                .willThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/99/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/comments - creates and returns new comment")
    void addComment_returnsCreated() throws Exception {
        given(commentService.addComment(eq(1L), any(CommentRequest.class)))
                .willReturn(sampleResponse());

        mockMvc.perform(post("/api/tasks/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great progress!"))
                .andExpect(jsonPath("$.taskId").value(1));
    }

    @Test
    @DisplayName("POST /api/tasks/{taskId}/comments - returns 404 when task not found")
    void addComment_taskNotFound_returns404() throws Exception {
        given(commentService.addComment(eq(99L), any(CommentRequest.class)))
                .willThrow(new TaskNotFoundException(99L));

        mockMvc.perform(post("/api/tasks/99/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/comments/{commentId} - returns 204 no content")
    void deleteComment_returnsNoContent() throws Exception {
        willDoNothing().given(commentService).deleteComment(1L);

        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/comments/{commentId} - returns 404 when comment not found")
    void deleteComment_notFound_returns404() throws Exception {
        willThrow(new ResourceNotFoundException("Comment", 99L))
                .given(commentService).deleteComment(99L);

        mockMvc.perform(delete("/api/comments/99"))
                .andExpect(status().isNotFound());
    }
}
