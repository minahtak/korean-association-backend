package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NoticeFormDto {

    private Long id;              // 게시글 번호 (수정할 때 필요)
    private String title;         // 제목
    private String content;       // 내용
    private String targetSchool;  // 학교
    private String category;      // 카테고리
    private Boolean isPinned;     // 상단 고정 여부 (true/false)

    // Lombok(@Getter, @Setter)이 있어서 getTitle(), setTitle() 등을 자동으로 만들어줍니다.
}