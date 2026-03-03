package com.student.iksu.controller;

import com.student.iksu.dto.request.CommentFormDto;
import com.student.iksu.entity.Comment;
import com.student.iksu.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class CommentApiController {

    private final CommentService commentService;

    // 댓글 목록 조회 API ---
    // 호출 예시: GET /api/comments?type=material&targetId=1
    @GetMapping
    public ResponseEntity<List<Comment>> getComments(
            @RequestParam String type,
            @RequestParam Long targetId) {

        List<Comment> comments = commentService.getComments(type, targetId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 등록
    @PostMapping
    public ResponseEntity<?> writeComment(@RequestBody CommentFormDto dto, @AuthenticationPrincipal User user) {
        try {
            if (user != null) {
                dto.setWriter(user.getUsername());
            } else if (dto.getWriter() == null || dto.getWriter().trim().isEmpty()) {
                dto.setWriter("익명");
            }

            commentService.writeComment(dto);
            return ResponseEntity.ok("댓글 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 등록 실패: " + e.getMessage());
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("STAFF"));

        try {
            commentService.deleteComment(id, user.getUsername(), isAdmin);
            return ResponseEntity.ok("댓글 삭제 성공");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 중 오류 발생");
        }
    }
}