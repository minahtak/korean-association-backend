package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class GalleryFormDto {

    private Long id;
    private String writer;
    private String title;
    private String content;

    // 다중 이미지 링크를 받기 위한 리스트
    private List<String> googleDriveLinks = new ArrayList<>();

    // 기존 이미지 URL들 (수정 시 사용)
    private String imageUrls;
}