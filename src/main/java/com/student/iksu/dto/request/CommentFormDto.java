package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentFormDto {
    private Long galleryId;
    private Long infoId; // (Info 게시판용)
    private Long materialId; // (material 용)

    private Long parentId; // 대댓글일 경우 부모 댓글 ID
    private String content;
    private String writer;

}