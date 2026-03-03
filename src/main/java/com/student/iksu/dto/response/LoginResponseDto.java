package com.student.iksu.dto.response;

import com.student.iksu.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;   // 발급된 JWT 토큰
    private Member member;  // 로그인한 회원 정보 (비밀번호는 빼고 주는 게 좋지만, 일단 Member 통째로 씁니다)
}