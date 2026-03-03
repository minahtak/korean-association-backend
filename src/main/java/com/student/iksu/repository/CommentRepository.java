package com.student.iksu.repository;

import com.student.iksu.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시판별로 부모 댓글이 없는(최상위) 댓글만 조회하는 메서드들
    // OrderByRegDateAsc: 작성 순서대로 정렬

    List<Comment> findByMaterialIdAndParentIsNullOrderByRegDateAsc(Long materialId);

    List<Comment> findByGalleryIdAndParentIsNullOrderByRegDateAsc(Long galleryId);

    List<Comment> findByInfoIdAndParentIsNullOrderByRegDateAsc(Long infoId);

}