package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "executive")
@Getter @Setter
@NoArgsConstructor
public class Executive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★ Member와 1:1 연결
    @OneToOne(fetch = FetchType.EAGER) // 즉시 로딩 (이름 가져와야 하므로)
    @JoinColumn(name = "member_id")
    private Member member;

    private String role; // 직책 (예: "Chairman")

    // ★ DB 컬럼은 유지하되, 실제 값은 Member에서 가져옴 (데이터 불일치 방지)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String school;

    @Column(length = 500)
    private String intro;

    @Column(nullable = false)
    private String imageUrl;

    public Executive(Member member, String role, String name, String school, String intro, String imageUrl) {
        this.member = member;
        this.role = role;
        this.name = name;     // 초기값 저장용
        this.school = school; // 초기값 저장용
        this.intro = intro;
        this.imageUrl = imageUrl;
    }

    // ★ [핵심] Getter 재정의: 멤버 정보가 바뀌면 여기도 자동으로 바뀜!
    public String getName() {
        return member != null ? member.getName() : this.name;
    }

    public String getSchool() {
        return member != null ? member.getSchool() : this.school;
    }
}