package com.student.iksu.dto.request;

import lombok.Data;

@Data
public class VoteSubmitDto {
    private String email;
    private String code;
    private Long candidateId;
}