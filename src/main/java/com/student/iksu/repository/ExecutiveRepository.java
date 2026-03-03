package com.student.iksu.repository;

import com.student.iksu.entity.Executive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutiveRepository extends JpaRepository<Executive, Long> {
    // memberId로 임원 찾기 (해제 시 필요)
    Executive findByMemberId(Long memberId);
}