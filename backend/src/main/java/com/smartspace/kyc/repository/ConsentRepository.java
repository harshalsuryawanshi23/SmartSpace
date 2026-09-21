package com.smartspace.kyc.repository;

import com.smartspace.kyc.entity.Consent;
import com.smartspace.kyc.entity.ConsentPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findFirstByUserIdAndPurposeAndRevokedAtIsNullOrderByGrantedAtDesc(Long userId, ConsentPurpose purpose);
}
