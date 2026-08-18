package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "voter_participation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_voter_email", columnNames = {"email"})
})
@Getter @Setter
@NoArgsConstructor
public class VoterParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 히브리대 이메일 (@mail.huji.ac.il)

    @Column(nullable = false)
    private LocalDateTime votedAt;

    public VoterParticipation(String email) {
        this.email = email;
        this.votedAt = LocalDateTime.now();
    }
}