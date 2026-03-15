package com.example.demo.service;

import com.example.demo.api.model.CommentRequest;
import com.example.demo.api.model.CommentResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.TaskNotFoundException;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.model.Task;
import com.example.demo.model.TaskComment;
import com.example.demo.repository.TaskCommentRepository;
import com.example.demo.repository.TaskRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
@DisplayName("CommentService")
class CommentServiceTest {

    @Mock
    private TaskCommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private Task sampleTask() {
        return Task.builder().id(1L).title("Test Task").build();
    }

    private TaskComment sampleComment() {
        return TaskComment.builder()
                .id(1L)
                .content("Great progress!")
                .authorName("Ankit")
                .createdAt(LocalDateTime.now())
                .task(sampleTask())
                .build();
    }

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
    @DisplayName("getCommentsByTaskId - returns mapped list when task exists")
    void getCommentsByTaskId_returnsList() {
        TaskComment comment = sampleComment();
        given(taskRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findByTaskId(1L)).willReturn(List.of(comment));
        given(commentMapper.toResponse(comment)).willReturn(sampleResponse());

        List<CommentResponse> result = commentService.getCommentsByTaskId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContent()).isEqualTo("Great progress!");
    }

    @Test
    @DisplayName("getCommentsByTaskId - throws TaskNotFoundException when task not found")
    void getCommentsByTaskId_throwsWhenTaskNotFound() {
        given(taskRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> commentService.getCommentsByTaskId(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("addComment - saves comment and returns mapped response")
    void addComment_savesAndReturns() {
        Task task = sampleTask();
        TaskComment saved = sampleComment();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(commentRepository.save(any(TaskComment.class))).willReturn(saved);
        given(commentMapper.toResponse(saved)).willReturn(sampleResponse());

        CommentResponse result = commentService.addComment(1L, sampleRequest());

        assertThat(result.getContent()).isEqualTo("Great progress!");
        assertThat(result.getTaskId()).isEqualTo(1L);
        then(commentRepository).should().save(any(TaskComment.class));
    }

    @Test
    @DisplayName("addComment - throws TaskNotFoundException when task not found")
    void addComment_throwsWhenTaskNotFound() {
        given(taskRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(99L, sampleRequest()))
                .isInstanceOf(TaskNotFoundException.class);
        then(commentRepository).should(never()).save(any(TaskComment.class));
    }

    @Test
    @DisplayName("deleteComment - deletes when comment exists")
    void deleteComment_deletesWhenExists() {
        given(commentRepository.existsById(1L)).willReturn(true);

        commentService.deleteComment(1L);

        then(commentRepository).should().deleteById(1L);
    }

    @Test
    @DisplayName("deleteComment - throws ResourceNotFoundException when not found")
    void deleteComment_throwsWhenNotFound() {
        given(commentRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> commentService.deleteComment(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        then(commentRepository).should(never()).deleteById(99L);
    }
}
