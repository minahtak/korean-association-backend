package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ExecutiveFormDto {
    private Long memberId; // 필수!
    private String role;
    private String intro;
    private String imageUrl;

    // 프론트에서 보내주긴 하지만, Service에서는 DB(Member)에 있는 값을 우선으로 씁니다.
    private String name;
    private String school;
}