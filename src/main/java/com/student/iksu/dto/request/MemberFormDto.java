package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class MemberFormDto {
    private String username; // 아이디
    private String email;
    private String password;
    private String name;
    private String school; // 히브리대, 텔아비브대 등

    // 화면에서 "2002-05-15" 처럼 문자열로 오면 자동으로 날짜로 바꿔줍니다!
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    // ★ 추가된 필드
    private String degreeLevel; // "BACHELOR", "MASTER" ...
    private List<String> majors; // 프론트에서 ["CS", "Math"] 형태로 받음

    private String role; // "USER" (일반), "ADMIN" (관리자)
}