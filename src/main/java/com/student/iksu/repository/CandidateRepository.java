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

    List<Candidate> findAllByOrderByCandidateNumberAsc();

    // 찬성표 1 증가
    @Modifying
    @Query("UPDATE Candidate c SET c.votes = c.votes + 1 WHERE c.id = :id")
    int incrementVoteCount(@Param("id") Long id);

    // ★ 추가: 반대표 1 증가
    @Modifying
    @Query("UPDATE Candidate c SET c.rejectVotes = c.rejectVotes + 1 WHERE c.id = :id")
    int incrementRejectVoteCount(@Param("id") Long id);
}