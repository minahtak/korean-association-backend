package com.student.iksu.repository;

import com.student.iksu.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    // ★ C콤 자료실 전용 만능 검색 쿼리
    // 학교, 전공, 언어, 번역여부, 검색어(제목+내용)를 모두 필터링
    @Query("SELECT m FROM Material m WHERE " +
            "(:school IS NULL OR m.school = :school) AND " +
            "(:major IS NULL OR m.major = :major) AND " +
            "(:language IS NULL OR m.language = :language) AND " +
            "(:translationType IS NULL OR m.translationType = :translationType) AND " +
            "(:keyword IS NULL OR m.title LIKE %:keyword% OR m.content LIKE %:keyword%)")
    Page<Material> findMaterials(@Param("school") String school,
                                 @Param("major") String major,
                                 @Param("language") String language,
                                 @Param("translationType") String translationType,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);
}