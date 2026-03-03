package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "likes", uniqueConstraints = {
        // 한 유저가 한 게시물에 중복으로 좋아요를 누르는 것을 DB 레벨에서 방지
        @UniqueConstraint(columnNames = {"member_id", "gallery_id"}),
        @UniqueConstraint(columnNames = {"member_id", "info_id"}),
        @UniqueConstraint(columnNames = {"member_id", "material_id"})
})
public class Likes {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // --- 게시판 종류별 연관관계 (하나만 값이 있고 나머지는 null) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gallery_id")
    private Gallery gallery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_id")
    private Info info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    // 생성자: 갤러리 좋아요용
    public Likes(Gallery gallery, Member member) {
        this.gallery = gallery;
        this.member = member;
    }

    // 생성자: 정보 게시판 좋아요용
    public Likes(Info info, Member member) {
        this.info = info;
        this.member = member;
    }

    // 생성자: 자료실 좋아요용
    public Likes(Material material, Member member) {
        this.material = material;
        this.member = member;
    }
}