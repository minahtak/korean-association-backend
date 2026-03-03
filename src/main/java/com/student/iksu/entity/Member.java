package com.student.iksu.entity;

import com.student.iksu.constant.DegreeLevel;
import com.student.iksu.constant.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime; // ★ 시간까지 저장하기 위해 추가

@Entity
@Table(name = "member")
@Getter @Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String school;

    @Enumerated(EnumType.STRING)
    private DegreeLevel degreeLevel;

    private String major1;
    private String major2;
    private String major3;

    private LocalDate birthDate;

    // ★ [추가됨] 가입일 (날짜 + 시간)
    // updatable = false: 가입일은 수정되지 않도록 설정
    @Column(updatable = false)
    private LocalDateTime joinDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 생성자 업데이트
    public Member(String username, String email, String password, String name, String school,
                  DegreeLevel degreeLevel, String major1, String major2, String major3,
                  LocalDate birthDate, Role role, LocalDateTime joinDate) { // joinDate 추가
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.school = school;
        this.degreeLevel = degreeLevel;
        this.major1 = major1;
        this.major2 = major2;
        this.major3 = major3;
        this.birthDate = birthDate;
        this.role = role;
        this.joinDate = joinDate; // ★ 저장
    }
}