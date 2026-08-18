package com.student.iksu.repository;

import com.student.iksu.entity.VoteVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteVerificationCodeRepository extends JpaRepository<VoteVerificationCode, Long> {
    Optional<VoteVerificationCode> findTopByEmailOrderByExpireAtDesc(String email);
    void deleteByEmail(String email);
}