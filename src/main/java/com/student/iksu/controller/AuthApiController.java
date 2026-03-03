package com.student.iksu.controller;

import com.student.iksu.dto.request.LoginRequestDto;
import com.student.iksu.dto.request.MemberFormDto;
import com.student.iksu.dto.response.LoginResponseDto; // 방금 만든 DTO
import com.student.iksu.entity.Member;
import com.student.iksu.jwt.JwtTokenProvider; // ★ 아까 만든 토큰 발급기 import
import com.student.iksu.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider; // ★ 주입 받기

    // 1. 회원가입 (변경 없음)
    @PostMapping("/join")
    public ResponseEntity<?> signup(@RequestBody MemberFormDto dto) {
        try {
            memberService.join(dto);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }

    // 2. 로그인 (★ 세션 -> JWT 로직으로 대변경)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        try {
            // 1. 아이디/비번 검증 (이건 똑같음)
            // 여기서 실패하면 바로 catch문으로 넘어감
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            // 2. 인증 정보를 기반으로 JWT 토큰 생성! (★ 핵심)
            String token = tokenProvider.createToken(authentication);

            // 3. 로그인한 회원 정보 가져오기 (프론트에 보여주기 위함)
            Member member = memberService.findMember(dto.getUsername());

            // 4. 토큰과 회원정보를 DTO에 담아서 리턴
            LoginResponseDto response = LoginResponseDto.builder()
                    .token(token)
                    .member(member)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("로그인 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    // 3. 로그아웃 (★ 서버는 할 일이 없음)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT 방식에서 로그아웃은 "프론트엔드"가 토큰을 버리면 끝입니다.
        // 서버에서는 딱히 세션을 지울 필요가 없습니다. (Stateless니까요)
        return ResponseEntity.ok("로그아웃 성공");
    }

    // 4. 내 정보 확인 (토큰으로 정보 조회)
    @GetMapping("/me")
    public ResponseEntity<?> checkAuth() {
        // JwtAuthenticationFilter가 헤더를 검사해서 SecurityContext에 인증 정보를 이미 넣어둠
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 토큰에 들어있던 username으로 회원정보 찾기
            String username = authentication.getName();
            Member member = memberService.findMember(username);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}