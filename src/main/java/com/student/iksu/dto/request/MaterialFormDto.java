package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MaterialFormDto {

    private Long id;              // 수정 시 필요

    // --- 기본 정보 ---
    private String title;         // 제목
    private String content;       // 설명
    private String googleDriveLink; // ★ 핵심: 구글 드라이브 링크

    // --- 분류 정보 ---
    private String school;        // 학교
    private String major;         // 전공
    private String subject;       // 과목명
    private String category;      // 자료 유형 (Note, Exam...)

    private String professor;

    // --- 언어 & 번역 정보 ---
    private String language;      // 자료 언어 (Korean, Hebrew...)
    private String translationType; // 번역 방식 (Original, AI...)

    private String writer;        // 실제 기여자 (작성자) 이름
}