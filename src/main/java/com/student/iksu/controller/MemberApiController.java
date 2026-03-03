package com.student.iksu.controller;

import com.student.iksu.dto.request.PasswordChangeDto;
import com.student.iksu.service.EmailService;
import com.student.iksu.service.MemberService;
import com.student.iksu.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.security.Principal;

import com.student.iksu.dto.request.MyPageUpdateDto;
import com.student.iksu.dto.request.AccountUpdateDto;

@RestController // HTML 파일이 아니라 '데이터'를 돌려주는 컨트롤러
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final EmailService emailService; // 이메일 서비스

    // 1. 아이디 중복 확인 API
    @GetMapping("/api/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(memberService.checkUsernameDuplicate(username));
    }

    // 2. 이메일 중복 확인 API
    @GetMapping("/api/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.checkEmailDuplicate(email));
    }

    // 3. 이메일 인증 번호 발송 API
    @PostMapping("/api/email/send-code")
    public ResponseEntity<String> sendEmailCode(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "JOIN") String type) { // type 파라미터 추가!
        try {
            if (memberService.checkEmailDuplicate(email)) {
                return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
            }

            // 프론트에서 넘겨준 type ("JOIN" 또는 "UPDATE")에 맞춰서 메일 발송
            emailService.sendVerificationCode(email, type);
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("이메일 발송에 실패했습니다.");
        }
    }

    // 4. 이메일 인증 번호 검증 API
    @PostMapping("/api/email/verify-code")
    public ResponseEntity<Boolean> verifyEmailCode(@RequestParam String email, @RequestParam String code) {
        // 이메일과 코드가 짝짜꿍이 맞는지 확인하고 true/false를 프론트로 던져줌
        boolean isVerified = emailService.verifyCode(email, code);
        return ResponseEntity.ok(isVerified);
    }

    // 5. 아이디 찾기 API
    @PostMapping("/api/auth/find-id")
    public ResponseEntity<String> findId(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String username = memberService.findUsernameByNameAndEmail(name, email);
            return ResponseEntity.ok(username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("일치하는 회원 정보가 없습니다.");
        }

    }

    // 6. 비밀번호 변경 처리
    // 1. 비밀번호 재설정을 위한 사용자 확인 및 인증코드 발송
    @PostMapping("/api/auth/reset-password/verify")
    public ResponseEntity<String> verifyForReset(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");

        Optional<Member> member = memberService.findByEmail(email);

        if (member.isPresent() && member.get().getUsername().equals(username)) {
            // 여기서는 무조건 "RESET" 타입을 전달!
            emailService.sendVerificationCode(email, "RESET");
            return ResponseEntity.ok("인증 코드가 발송되었습니다.");
        }
        return ResponseEntity.badRequest().body("입력하신 정보와 일치하는 회원이 없습니다.");
    }


    // 2. 인증코드 검증 및 비밀번호 변경 완료
    @PostMapping("/api/auth/reset-password/complete")
    public ResponseEntity<String> completeReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        if (emailService.verifyCode(email, code)) {
            memberService.updatePassword(email, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 틀렸거나 만료되었습니다.");
    }

    // ==========================================
    // 마이페이지 전용 API
    // ==========================================

    // 1. 기본 정보 수정 API
    @PutMapping("/api/mypage/info")
    public ResponseEntity<?> updateInfo(Principal principal, @RequestBody MyPageUpdateDto dto) {
        try {
            Member updatedMember = memberService.updateMemberInfo(principal.getName(), dto);
            return ResponseEntity.ok(updatedMember); // 수정된 회원 정보 반환
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 계정 정보 수정 API
    @PutMapping("/api/mypage/account")
    public ResponseEntity<?> updateAccount(Principal principal, @RequestBody AccountUpdateDto dto) {
        try {
            Member updatedMember = memberService.updateAccountInfo(principal.getName(), dto);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 비밀번호 변경 API
    @PutMapping("/api/mypage/password")
    public ResponseEntity<?> changePassword(Principal principal, @RequestBody PasswordChangeDto dto) {
        try {
            memberService.changePasswordWithCurrent(principal.getName(), dto);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 회원 탈퇴 API
    @DeleteMapping("/api/mypage/withdraw")
    public ResponseEntity<?> withdraw(Principal principal, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            memberService.withdrawMember(principal.getName(), password);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}