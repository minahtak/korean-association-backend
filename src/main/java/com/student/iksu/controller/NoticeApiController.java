package com.student.iksu.controller;

import com.student.iksu.dto.request.NoticeFormDto;
import com.student.iksu.entity.Member;
import com.student.iksu.entity.Notice;
import com.student.iksu.repository.MemberRepository;
import com.student.iksu.repository.NoticeRepository;
import com.student.iksu.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeApiController {

    private final NoticeService noticeService;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // 1. 공지사항 목록 조회 (검색, 필터, 페이징, 정렬 완벽 구현)
    @GetMapping
    public ResponseEntity<Page<Notice>> getNotices(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "LATEST") String sort,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        // 정렬 기준 설정 (기존 Controller 로직 이식)
        Sort sortOption;
        if ("VIEWS".equalsIgnoreCase(sort)) {
            sortOption = Sort.by("isPinned").descending()
                    .and(Sort.by("viewCount").descending())
                    .and(Sort.by("regDate").descending());
        } else {
            sortOption = Sort.by("isPinned").descending()
                    .and(Sort.by("regDate").descending());
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        // 검색 로직은 Service에 이미 구현되어 있다고 가정 (기존 Controller와 동일하게 호출)
        // 만약 Service 메서드 시그니처가 다르면 맞춰줘야 함.
        Page<Notice> notices = noticeService.getNoticeList(school, category, keyword, sortedPageable);

        return ResponseEntity.ok(notices);
    }

    // 2. 상세 조회 (조회수 증가 포함)
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeDetail(@PathVariable Long id) {
        try {
            noticeService.updateViewCount(id); // 조회수 증가
            Notice notice = noticeService.findById(id);
            return ResponseEntity.ok(notice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. 공지사항 작성 (권한 체크는 SecurityConfig에서 처리됨을 가정)
    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeFormDto dto, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");

        try {
            Member writer = memberRepository.findByUsername(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

            Notice notice = new Notice();
            notice.setTitle(dto.getTitle());
            notice.setContent(dto.getContent());
            notice.setCategory(dto.getCategory());
            notice.setTargetSchool(dto.getTargetSchool());
            notice.setWriter(writer.getName()); // 작성자 이름 저장
            notice.setRegDate(LocalDateTime.now());
            notice.setViewCount(0);
            notice.setPinned(dto.getIsPinned() != null && dto.getIsPinned());

            noticeRepository.save(notice);
            return ResponseEntity.ok("공지사항이 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }

    // 4. 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(@PathVariable Long id, @RequestBody NoticeFormDto dto, @AuthenticationPrincipal User user) {
        try {
            Notice notice = noticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글 없음"));
            Member currentMember = memberRepository.findByUsername(user.getUsername()).orElseThrow();

            // 본인 글이거나 관리자만 수정 가능 (엄격한 체크)
            // 참고: 보통 관리자는 모든 글 수정 가능하도록 함
            boolean isWriter = notice.getWriter().equals(currentMember.getName());
            boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

            if (!isWriter && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
            }

            notice.updateNotice(
                    dto.getTitle(),
                    dto.getContent(),
                    dto.getTargetSchool(),
                    dto.getCategory(),
                    dto.getIsPinned() != null && dto.getIsPinned()
            );
            noticeRepository.save(notice);

            return ResponseEntity.ok("수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("수정 실패: " + e.getMessage());
        }
    }

    // 5. 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            Notice notice = noticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글 없음"));
            Member currentMember = memberRepository.findByUsername(user.getUsername()).orElseThrow();

            boolean isWriter = notice.getWriter().equals(currentMember.getName());
            boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isWriter || isAdmin || isStaff) {
                noticeRepository.delete(notice);
                return ResponseEntity.ok("삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
        }
    }
}