package com.student.iksu.dto.response;

import com.student.iksu.entity.Candidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CandidateResponseDto {
    private Long id;
    private Integer candidateNumber;
    private String name;
    private String school;
    private String slogan;
    private String imageUrl;
    private List<String> pledges;
    private Integer votes;

    public static CandidateResponseDto from(Candidate candidate) {
        return CandidateResponseDto.builder()
                .id(candidate.getId())
                .candidateNumber(candidate.getCandidateNumber())
                .name(candidate.getName())
                .school(candidate.getSchool())
                .slogan(candidate.getSlogan())
                .imageUrl(candidate.getImageUrl())
                .pledges(candidate.getPledges())
                .votes(candidate.getVotes())
                .build();
    }
}