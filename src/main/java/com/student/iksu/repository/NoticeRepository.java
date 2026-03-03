package com.student.iksu.repository;

import com.student.iksu.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 1. 전체 조회
    List<Notice> findAllByOrderByIsPinnedDescRegDateDesc();

    // 2. 카테고리별 조회
    List<Notice> findByCategoryOrderByIsPinnedDescRegDateDesc(String category);

    // 3. 학교별 조회
    List<Notice> findByTargetSchoolOrderByIsPinnedDescRegDateDesc(String targetSchool);

    // 4. 검색 필터 (★ 학교 및 카테고리 '기타' 조건 처리 추가)
    @Query("SELECT n FROM Notice n WHERE " +
            "(:school IS NULL " +
            "   OR (:school = 'ETC' AND n.targetSchool NOT IN ('히브리대', '텔아비브대', '테크니온', '바일란대', 'All')) " +
            "   OR (:school != 'ETC' AND n.targetSchool = :school)) AND " +
            "(:category IS NULL " +
            "   OR (:category = 'Etc' AND n.category NOT IN ('공지', '행사', '비자', '긴급', 'ALL')) " +
            "   OR (:category != 'Etc' AND n.category = :category)) AND " +
            "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    Page<Notice> findNotices(@Param("school") String school,
                             @Param("category") String category,
                             @Param("keyword") String keyword,
                             Pageable pageable);
}