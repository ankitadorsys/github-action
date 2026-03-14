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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;

    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        return commentRepository.findByTaskId(taskId).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskComment comment = TaskComment.builder()
                .content(request.getContent())
                .authorName(request.getAuthorName())
                .task(task)
                .build();

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        commentRepository.deleteById(commentId);
    }
}
