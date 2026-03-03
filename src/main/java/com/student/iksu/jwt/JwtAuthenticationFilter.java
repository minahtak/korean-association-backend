package com.student.iksu.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검사 (유효하다면 인증 정보를 SecurityContext에 저장)
        if (token != null && tokenProvider.validateToken(token)) {
            // 토큰이 유효하면 토큰에서 유저 정보를 가져옵니다.
            Authentication authentication = tokenProvider.getAuthentication(token);
            // SecurityContext 에 유저 정보를 저장합니다. (이제부터 전역적으로 로그인 된 상태로 인지함)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 3. 다음 필터로 이동 (검문 통과!)
        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열만 가져옴
        }
        return null;
    }
}