package com.student.iksu.entity;

import com.student.iksu.constant.InfoCategory;
import com.student.iksu.constant.InfoStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Info {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String writer; // 작성자

    @Enumerated(EnumType.STRING)
    private InfoCategory category; // 카테고리

    @Enumerated(EnumType.STRING)
    private InfoStatus status; // 승인 상태 (PENDING, APPROVED...)

    private String rejectionReason; // 반려 사유

    // --- 태그 시스템 ---
    private String schoolTag; // #HebrewU 등
    private String targetTag; // #신입 등

    private int viewCount;
    private LocalDateTime regDate;

    // 댓글 연결
    @OneToMany(mappedBy = "info", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("regDate asc")
    @SQLRestriction("parent_id is null") // ★ 부모 없는 댓글만 가져오기 (필터링)
    @JsonIgnoreProperties({"info", "gallery", "material", "parent"}) // ★ 무한 루프 방지
    private List<Comment> comments = new ArrayList<>();
}