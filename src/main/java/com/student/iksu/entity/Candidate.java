package com.student.iksu.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidate")
@Getter @Setter
@NoArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer candidateNumber; // 기호 1번

    @Column(nullable = false)
    private String name;

    private String school;

    private String slogan;

    @Column(length = 1000)
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_pledges", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "pledge")
    private List<String> pledges = new ArrayList<>();

    @Column(nullable = false)
    private Integer votes = 0; // ★ 찬성표

    @Column(nullable = false)
    private Integer rejectVotes = 0; // ★ 추가: 반대표

    public Candidate(Integer candidateNumber, String name, String school, String slogan, String imageUrl, List<String> pledges) {
        this.candidateNumber = candidateNumber;
        this.name = name;
        this.school = school;
        this.slogan = slogan;
        this.imageUrl = imageUrl;
        this.pledges = pledges != null ? pledges : new ArrayList<>();
        this.votes = 0;
        this.rejectVotes = 0;
    }
}