package com.student.iksu.controller;

import com.student.iksu.constant.InfoStatus;
import com.student.iksu.dto.request.InfoFormDto;
import com.student.iksu.entity.Info;
import com.student.iksu.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/infos")
@RequiredArgsConstructor
// Admin과 달리 조회는 누구나 가능해야 하므로 클래스 레벨 @PreAuthorize는 뺍니다.
public class InfoApiController {
    private final InfoService infoService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<Page<Info>> getInfos(
            @RequestParam(required = false) String schoolTag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User user, // 로그인 정보
            @PageableDefault(page = 0, size = 9, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. 사용자 정보 추출
        String username = (user != null) ? user.getUsername() : null;

        // 2. 관리자 여부 체크
        boolean isAdmin = false;
        if (user != null) {
            isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN") ||
                            a.getAuthority().equals("STAFF") || a.getAuthority().equals("ROLE_STAFF"));
        }

        // 3. 서비스 호출 (status 대신 username, isAdmin 전달)
        return ResponseEntity.ok(infoService.getInfoList(schoolTag, category, keyword, status, username, isAdmin, pageable));
    }

    // 2. 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<Info> getDetail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(infoService.getInfo(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. 정보 등록
    @PostMapping
    public ResponseEntity<?> create(@RequestBody InfoFormDto dto, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        try {
            infoService.saveInfo(dto, user);

            // 메시지 처리
            boolean isManager = isManager(user);
            String msg = isManager ? "등록되었습니다." : "등록되었습니다. 관리자 승인 후 공개됩니다.";

            // AdminController처럼 간단한 메시지나 Map 반환
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InfoFormDto dto, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();

        // 권한 체크는 Service 혹은 여기서 수행
        Info info = infoService.getInfo(id);
        if (!info.getWriter().equals(user.getUsername()) && !isManager(user)) {
            return ResponseEntity.status(403).body("수정 권한이 없습니다.");
        }

        try {
            infoService.updateInfo(id, dto);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("수정 실패: " + e.getMessage());
        }
    }

    // 5. 정보 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();

        Info info = infoService.getInfo(id);
        if (!info.getWriter().equals(user.getUsername()) && !isManager(user)) {
            return ResponseEntity.status(403).body("삭제 권한이 없습니다.");
        }

        try {
            infoService.deleteInfo(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제 실패");
        }
    }

    // 6. 승인/반려 (운영진 전용)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestParam InfoStatus status,
            @RequestParam(required = false) String reason) {

        infoService.approveInfo(id, status, reason);
        return ResponseEntity.ok("상태 변경 완료: " + status);
    }

    // 헬퍼 메서드
    private boolean isManager(User user) {
        return user.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN") ||
                                a.getAuthority().equals("STAFF") || a.getAuthority().equals("ROLE_STAFF")
                );
    }
}