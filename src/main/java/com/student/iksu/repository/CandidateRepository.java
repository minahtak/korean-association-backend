package com.student.iksu.repository;

import com.student.iksu.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // 기호 순서대로 후보자 목록 정렬 조회
    List<Candidate> findAllByOrderByCandidateNumberAsc();

    // 동시성 안전한 득표수 1 증가 쿼리
    @Modifying
    @Query("UPDATE Candidate c SET c.votes = c.votes + 1 WHERE c.id = :id")
    int incrementVoteCount(@Param("id") Long id);
}