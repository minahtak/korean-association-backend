package com.student.iksu.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String writer;
    private LocalDateTime regDate;

    // --- 게시글 연결 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id")
    @JsonIgnoreProperties("comments") // 갤러리 안의 댓글 리스트 무시
    private Gallery gallery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_id")
    @JsonIgnoreProperties("comments") // 인포 안의 댓글 리스트 무시
    private Info info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    @JsonIgnoreProperties("comments") // 자료 안의 댓글 리스트 무시 (중요!)
    private Material material;

    // --- 대댓글 구조 (핵심) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference // 자식 -> 부모 방향은 직렬화 하지 않음 (루프 방지)
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("regDate asc")
    @JsonManagedReference // 부모 -> 자식 방향은 직렬화 함
    private List<Comment> children = new ArrayList<>();
}