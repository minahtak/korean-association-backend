package com.student.iksu.repository;

import com.student.iksu.constant.InfoCategory;
import com.student.iksu.entity.Info;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InfoRepository extends JpaRepository<Info, Long> {

    // 관리자 권한 + 본인 글 보기 로직이 포함된 쿼리
    @Query("SELECT i FROM Info i WHERE " +
            "(:schoolTag IS NULL OR i.schoolTag = :schoolTag) AND " +
            "(:category IS NULL OR i.category = :category) AND " +
            "(:keyword IS NULL OR i.title LIKE %:keyword% OR i.content LIKE %:keyword%) AND " +
            "(:status IS NULL OR CAST(i.status AS string) = :status) AND " + // ★ 추가: 넘어온 status가 있으면 해당 상태만 검색
            "(" +
            "   :isAdmin = true " +
            "   OR i.status = 'APPROVED' " +
            "   OR (:username IS NOT NULL AND i.writer = :username)" +
            ")")
    Page<Info> findInfosCustom(
            @Param("schoolTag") String schoolTag,
            @Param("category") InfoCategory category,
            @Param("keyword") String keyword,
            @Param("status") String status, // ★ 추가
            @Param("username") String username,
            @Param("isAdmin") boolean isAdmin,
            Pageable pageable);
}