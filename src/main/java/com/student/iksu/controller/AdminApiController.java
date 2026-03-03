package com.student.iksu.controller;

import com.student.iksu.constant.Role;
import com.student.iksu.dto.request.ExecutiveFormDto;
import com.student.iksu.entity.Executive;
import com.student.iksu.entity.Member;
import com.student.iksu.repository.MemberRepository;
import com.student.iksu.service.ExecutiveService;
import com.student.iksu.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
// "ADMIN" 또는 "STAFF" 글자를 정확히 가진 사람만 통과
@PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
public class AdminApiController {

    private final MemberService memberService;
    private final ExecutiveService executiveService;
    private final MemberRepository memberRepository;

    // 1. 통계
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMembers", memberRepository.count());
        stats.put("executives", (long) executiveService.getAllExecutives().size());
        stats.put("pendingWiki", 0L);
        stats.put("pendingMaterials", 0L);
        return ResponseEntity.ok(stats);
    }

    // 2. 회원 목록
    @GetMapping("/members")
    public ResponseEntity<Page<Member>> getMembers(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Role filterRole = null;
        if (role != null && !role.equals("ALL")) {
            try { filterRole = Role.valueOf(role); } catch (Exception e) {}
        }
        String searchText = (search == null || search.trim().isEmpty()) ? null : search.trim();
        String currentUsername = (user != null) ? user.getUsername() : "";

        return ResponseEntity.ok(memberRepository.findMembersWithSearch(currentUsername, filterRole, searchText, pageable));
    }

    // 3. 회원 삭제
    @DeleteMapping("/members/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable Long id) {
        try {
            executiveService.deleteExecutiveByMemberId(id);
            memberRepository.deleteById(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("삭제 실패: " + e.getMessage());
        }
    }

    // 4. 임원 목록
    @GetMapping("/executives")
    public ResponseEntity<List<Executive>> getExecutives() {
        return ResponseEntity.ok(executiveService.getAllExecutives());
    }

    // 5. 임원 승격 (Service로 로직 이임)
    @PostMapping("/promote")
    public ResponseEntity<String> promoteMember(@RequestBody ExecutiveFormDto formDto) {
        try {
            if (formDto.getMemberId() == null) {
                return ResponseEntity.badRequest().body("회원 ID가 누락되었습니다.");
            }
            // Service가 트랜잭션 안에서 안전하게 처리
            executiveService.promoteMember(formDto.getMemberId(), formDto);

            return ResponseEntity.ok("승격 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("승격 실패: " + e.getMessage());
        }
    }

    // 6. 임원 수정
    @PutMapping("/executives/{id}")
    public ResponseEntity<String> updateExecutive(@PathVariable Long id, @RequestBody ExecutiveFormDto formDto) {
        try {
            executiveService.updateExecutive(id, formDto);
            return ResponseEntity.ok("수정 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("수정 실패: " + e.getMessage());
        }
    }

    // 7. 임원 해제
    @DeleteMapping("/revoke/{memberId}")
    public ResponseEntity<String> revokeExecutive(@PathVariable Long memberId) {
        try {
            executiveService.deleteExecutiveByMemberId(memberId);
            memberService.changeMemberRole(memberId, Role.USER);
            return ResponseEntity.ok("해제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("해제 실패: " + e.getMessage());
        }
    }

    // 8. 단순 권한 변경
    @PostMapping("/members/{id}/role")
    public ResponseEntity<String> changeRole(@PathVariable Long id, @RequestParam Role role) {
        try {
            memberService.changeMemberRole(id, role);
            return ResponseEntity.ok("변경 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("변경 실패");
        }
    }
}