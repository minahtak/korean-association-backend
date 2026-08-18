package com.student.iksu.repository;

import com.student.iksu.entity.VoterParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoterParticipationRepository extends JpaRepository<VoterParticipation, Long> {
    boolean existsByEmail(String email);
    Optional<VoterParticipation> findByEmail(String email);
}