package com.student.iksu.repository;

import com.student.iksu.entity.Gallery;
import com.student.iksu.entity.Info;
import com.student.iksu.entity.Likes;
import com.student.iksu.entity.Material;
import com.student.iksu.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    // 갤러리 좋아요 확인
    Optional<Likes> findByGalleryAndMember(Gallery gallery, Member member);
    boolean existsByGalleryAndMember(Gallery gallery, Member member);

    // 정보 게시판 좋아요 확인 (추후 사용)
    Optional<Likes> findByInfoAndMember(Info info, Member member);
    boolean existsByInfoAndMember(Info info, Member member);

    // 자료실 좋아요 확인 (추후 사용)
    Optional<Likes> findByMaterialAndMember(Material material, Member member);
    boolean existsByMaterialAndMember(Material material, Member member);
}