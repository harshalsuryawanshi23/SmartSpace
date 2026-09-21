package com.smartspace.auth.repository;

import com.smartspace.auth.entity.OtpChallenge;
import com.smartspace.auth.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {
    Optional<OtpChallenge> findByPublicId(String publicId);
    Optional<OtpChallenge> findTopByTargetAndPurposeOrderByCreatedAtDesc(String target, OtpPurpose purpose);
}
