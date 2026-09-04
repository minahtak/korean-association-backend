package com.student.iksu.dto.request;

import lombok.Data;

@Data
public class VoteSubmitDto {
    private String email;
    private String code;
    private Long candidateId;
    private String choice; // ★ 추가: "APPROVE" (찬성) 또는 "REJECT" (반대)
}