package com.student.iksu.repository;

import com.student.iksu.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.student.iksu.constant.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 1. 아이디로 회원 찾기 (로그인용) - ★ 변경됨
    Optional<Member> findByUsername(String username);

    // 2. 아이디 중복 체크용 - ★ 추가됨
    boolean existsByUsername(String username);

    // 1. 이메일로 회원 정보 찾기 (로그인용)
    Optional<Member> findByEmail(String email);

    // 2. 이미 가입된 이메일인지 확인하기 (회원가입 중복 체크용)
    boolean existsByEmail(String email);

    // ★ [추가] 역할별 조회 (페이징 지원)
    Page<Member> findByRole(Role role, Pageable pageable);

    // ★ [추가] 통계용 카운트
    long countByRole(Role role);

    // 1. role이 'ALL'이면(null) 역할 상관없이 검색
    // 2. 검색어(search)가 있으면 이름/아이디/학교 중에서 찾음
    @Query("SELECT m FROM Member m WHERE " +
            "m.username <> :currentUsername AND " + // 나 자신 제외
            "m.role <> 'ADMIN' AND " +              // 다른 관리자도 제외 (원하시면 제거 가능)
            "(:role IS NULL OR m.role = :role) AND " +
            "(:search IS NULL OR m.name LIKE %:search% OR m.username LIKE %:search% OR m.school LIKE %:search%)")
    Page<Member> findMembersWithSearch(
            @Param("currentUsername") String currentUsername,
            @Param("role") Role role,
            @Param("search") String search,
            Pageable pageable);

    // 이름과 이메일로 회원 찾기 (아이디 찾기용)
    Optional<Member> findByNameAndEmail(String name, String email);
}