package com.example.demo.controller;

import com.example.demo.api.CommentsApi;
import com.example.demo.api.model.CommentRequest;
import com.example.demo.api.model.CommentResponse;
import com.example.demo.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentsApi {

    private final CommentService commentService;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<CommentResponse>> getCommentsByTaskId(Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<CommentResponse> addComment(Long taskId, CommentRequest commentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(taskId, commentRequest));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteComment(Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
