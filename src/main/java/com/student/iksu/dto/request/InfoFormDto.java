package com.student.iksu.dto.request;

import com.student.iksu.constant.InfoCategory;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InfoFormDto {
    private Long id;
    private String title;
    private String content;
    private InfoCategory category;
    private String schoolTag;
    private String targetTag;
}