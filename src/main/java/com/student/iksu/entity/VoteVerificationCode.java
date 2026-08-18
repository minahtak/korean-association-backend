package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_verification_code")
@Getter @Setter
@NoArgsConstructor
public class VoteVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code; // 6자리 난수

    @Column(nullable = false)
    private LocalDateTime expireAt;

    private boolean verified = false; // 인증 성공 여부

    public VoteVerificationCode(String email, String code, int expireMinutes) {
        this.email = email;
        this.code = code;
        this.expireAt = LocalDateTime.now().plusMinutes(expireMinutes);
        this.verified = false;
    }
}