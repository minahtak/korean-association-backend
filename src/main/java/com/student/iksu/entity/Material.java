package com.student.iksu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Material {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- 1. 기본 정보 ---
    private String title;         // 제목

    @Column(columnDefinition = "TEXT")
    private String content;       // 설명

    private String writer;        // 작성자 (임원)

    // --- 2. 분류 필터 ---
    private String school;        // 학교
    private String major;         // 전공
    private String subject;       // 과목명
    private String category;      // 유형 (Note, Exam...)

    // --- 3. 언어 & 번역 ---
    private String language;      // 언어
    private boolean isTranslated; // 번역 여부
    private String translationType; // 번역 방식
    private String originalLang;  // 원본 언어

    // --- 4. 링크 ---
    @Column(columnDefinition = "TEXT")
    private String googleDriveLink;

    // --- 5. 관리 정보 ---
    private LocalDateTime regDate;
    private LocalDateTime updateDate;
    private int viewCount;
    private String professor;

    // --- 6. 댓글 리스트 ---
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("regDate asc")
    // @SQLRestriction("parent_id is null") // 이 줄을 반드시 추가해야 중복이 사라짐
    @Where(clause = "parent_id is null")
    // ★ JSON 에러 방지: 댓글 안의 'material', 'info', 'gallery', 'parent' 필드는 직렬화 제외
    @JsonIgnoreProperties({"material", "info", "gallery", "parent"})
    private List<Comment> comments = new ArrayList<>();

    // 편의 메서드: 업데이트용
    public void updateMaterial(String title, String content, String googleDriveLink,
                               String school, String major, String subject, String professor,
                               String language, String translationType) {
        this.title = title;
        this.content = content;
        this.googleDriveLink = googleDriveLink;
        this.school = school;
        this.major = major;
        this.subject = subject;
        this.professor = professor;
        this.language = language;
        this.translationType = translationType;
        this.updateDate = LocalDateTime.now();
    }
}