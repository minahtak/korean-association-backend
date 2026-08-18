package com.student.iksu.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CandidateFormDto {
    private Integer candidateNumber;
    private String name;
    private String school;
    private String slogan;
    private List<String> pledges;
    private String imageUrl;
}