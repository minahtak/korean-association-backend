package com.student.iksu.controller;

import com.student.iksu.dto.request.VoteSubmitDto;
import com.student.iksu.dto.response.CandidateResponseDto;
import com.student.iksu.service.ElectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/election")
@RequiredArgsConstructor
public class ElectionApiController {

    private final ElectionService electionService;

    // 1. 후보자 목록 조회
    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateResponseDto>> getCandidates() {
        return ResponseEntity.ok(electionService.getCandidates());
    }

    // ★ 2. 실시간 찬/반 집계 통계 조회 (추가됨)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getVoteStats() {
        return ResponseEntity.ok(electionService.getVoteStats());
    }

    // 3. 인증코드 발송
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVoteCode(@RequestParam String email) {
        try {
            electionService.sendVoteEmailCode(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "인증 코드가 발송되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("메일 발송 에러", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "메일 발송에 실패했습니다."));
        }
    }

    // 4. 인증코드 검증
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyVoteCode(@RequestParam String email, @RequestParam String code) {
        try {
            boolean isValid = electionService.verifyVoteCode(email, code);
            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 5. 최종 찬/반 투표 제출
    @PostMapping("/vote")
    public ResponseEntity<?> castVote(@RequestBody VoteSubmitDto dto) {
        try {
            electionService.submitVote(dto);
            return ResponseEntity.ok(Map.of("success", true, "message", "투표가 성공적으로 완료되었습니다."));
        } catch (Exception e) {
            log.error("투표 제출 에러", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 6. 투표 여부 확인
    @GetMapping("/check")
    public ResponseEntity<?> checkStatus(@RequestParam String email) {
        boolean hasVoted = electionService.checkHasVoted(email);
        return ResponseEntity.ok(Map.of("hasVoted", hasVoted));
    }
}