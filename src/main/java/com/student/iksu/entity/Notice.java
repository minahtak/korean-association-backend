package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor // 기본 생성자 (JPA 필수)
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;      // 말머리
    private String targetSchool;  // 대상 학교
    private boolean isPinned;     // 상단 고정 여부

    private String title;         // 제목

    @Column(columnDefinition = "TEXT")
    private String content;       // 내용

    private String writer;        // 작성자

    private LocalDateTime regDate;    // 작성일
    private LocalDateTime updateDate; // 수정일

    private int viewCount;        // 조회수

    // ★ [추가된 부분] 이 생성자가 없어서 에러가 났던 것입니다!
    // DataInitializer에서 호출하는 순서와 타입에 맞춰서 만들어줍니다.
    public Notice(String title, String content, String category, String targetSchool, String writer, LocalDateTime regDate, boolean isPinned, int viewCount) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.targetSchool = targetSchool;
        this.writer = writer;
        this.regDate = regDate;
        this.isPinned = isPinned;
        this.viewCount = viewCount;
    }

    // 수정 편의 메서드
    public void updateNotice(String title, String content, String targetSchool, String category, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.targetSchool = targetSchool;
        this.category = category;
        this.isPinned = isPinned;
        this.updateDate = LocalDateTime.now();
    }
}