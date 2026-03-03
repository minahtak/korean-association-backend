package com.student.iksu.controller;

import com.student.iksu.dto.request.GalleryFormDto;
import com.student.iksu.entity.Gallery;
import com.student.iksu.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class GalleryApiController {

    private final GalleryService galleryService;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.public-domain}")
    private String publicDomain;

    // 1. R2 업로드용 Pre-signed URL 발급
    @PostMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        // 프론트엔드에서 보내주는 파일 타입을 받습니다. (없으면 기본값으로 image/jpeg 설정)
        String contentType = body.getOrDefault("contentType", "image/jpeg");

        if (fileName == null) return ResponseEntity.badRequest().body("파일 이름이 필요합니다.");

        String objectKey = UUID.randomUUID().toString() + "_" + fileName;

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType) // 하드코딩 대신 동적으로 적용
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
            String fileUrl = publicDomain + "/" + objectKey;

            return ResponseEntity.ok(Map.of(
                    "uploadUrl", uploadUrl,
                    "fileUrl", fileUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("URL 발급 실패: " + e.getMessage());
        }
    }

    // 2. 목록 조회 (로그인 시 좋아요 여부 확인 위해 user 정보 받음)
    @GetMapping
    public ResponseEntity<Page<Gallery>> list(
            @PageableDefault(page = 0, size = 12, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {

        // 로그인 안 했으면 null, 했으면 username 전달
        String username = (user != null) ? user.getUsername() : null;
        return ResponseEntity.ok(galleryService.getGalleryList(pageable, username));
    }

    // 3. 상세 조회 (로그인 시 좋아요 여부 확인 위해 user 정보 받음)
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            galleryService.updateViewCount(id);
            String username = (user != null) ? user.getUsername() : null;
            return ResponseEntity.ok(galleryService.findById(id, username));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. 게시글 등록
    @PostMapping
    public ResponseEntity<?> write(@RequestBody GalleryFormDto dto, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            dto.setWriter(user.getUsername());
            galleryService.saveGallery(dto);
            return ResponseEntity.ok(Map.of("message", "갤러리가 등록되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }

    // 5. 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GalleryFormDto dto, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        try {
            Gallery gallery = galleryService.findById(id, null); // 권한 체크용 단순 조회

            if (!gallery.getWriter().equals(user.getUsername()) && !isManager(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }

            galleryService.update(id, dto);
            return ResponseEntity.ok(Map.of("message", "수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("수정 실패: " + e.getMessage());
        }
    }

    // 6. 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        try {
            Gallery gallery = galleryService.findById(id, null); // 권한 체크용 단순 조회

            if (!gallery.getWriter().equals(user.getUsername()) && !isManager(user)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            galleryService.delete(id);
            return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
        }
    }

    // 7. 좋아요 (토글 방식)
    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // increaseLikes 대신 toggleLike 사용
            boolean isLiked = galleryService.toggleLike(id, user.getUsername());
            return ResponseEntity.ok(Map.of(
                    "liked", isLiked,
                    "message", isLiked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- Helper Method ---
    private boolean isManager(User user) {
        return user.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("STAFF") || a.getAuthority().equals("ROLE_STAFF")
                );
    }
}