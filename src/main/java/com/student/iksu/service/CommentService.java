package com.student.iksu.service;

import com.student.iksu.dto.request.CommentFormDto;
import com.student.iksu.entity.Comment;
import com.student.iksu.entity.Gallery;
import com.student.iksu.entity.Info;
import com.student.iksu.entity.Material;
import com.student.iksu.repository.CommentRepository;
import com.student.iksu.repository.GalleryRepository;
import com.student.iksu.repository.InfoRepository;
import com.student.iksu.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final GalleryRepository galleryRepository;
    private final MaterialRepository materialRepository;
    private final InfoRepository infoRepository;

    // 부모가 없는(최상위) 댓글만 가져옵니다. 자식 댓글은 Entity 관계 설정을 통해 자동으로 포함됩니다.
    @Transactional(readOnly = true)
    public List<Comment> getComments(String type, Long targetId) {
        if ("material".equals(type)) {
            return commentRepository.findByMaterialIdAndParentIsNullOrderByRegDateAsc(targetId);
        } else if ("gallery".equals(type)) {
            return commentRepository.findByGalleryIdAndParentIsNullOrderByRegDateAsc(targetId);
        } else if ("info".equals(type)) {
            return commentRepository.findByInfoIdAndParentIsNullOrderByRegDateAsc(targetId);
        } else {
            throw new IllegalArgumentException("잘못된 게시판 타입입니다.");
        }
    }

    public void writeComment(CommentFormDto dto) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setWriter(dto.getWriter());
        comment.setRegDate(LocalDateTime.now());

        // 1. 게시판 타입별 연결
        if (dto.getGalleryId() != null) {
            Gallery gallery = galleryRepository.findById(dto.getGalleryId())
                    .orElseThrow(() -> new IllegalArgumentException("갤러리 게시글이 없습니다."));
            comment.setGallery(gallery);
        } else if (dto.getInfoId() != null) {
            Info info = infoRepository.findById(dto.getInfoId())
                    .orElseThrow(() -> new IllegalArgumentException("정보 게시글이 없습니다."));
            comment.setInfo(info);
        } else if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new IllegalArgumentException("학술 자료가 없습니다."));
            comment.setMaterial(material);
        } else {
            throw new IllegalArgumentException("댓글이 달릴 게시글 정보가 없습니다.");
        }

        // 2. 대댓글 처리 (부모 연결)
        if (dto.getParentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 없습니다."));
            comment.setParent(parent);
        }

        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId, String username, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (comment.getWriter().equals(username) || isAdmin) {
            commentRepository.delete(comment);
        } else {
            throw new SecurityException("본인의 댓글만 삭제할 수 있습니다.");
        }
    }
}