package com.smartspace.kyc.repository;

import com.smartspace.kyc.entity.KycStatus;
import com.smartspace.kyc.entity.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {

    @Query("SELECT k FROM KycVerification k WHERE k.user.id = :userId AND k.status IN :statuses ORDER BY k.createdAt DESC LIMIT 1")
    Optional<KycVerification> findLatestByUserIdAndStatusIn(Long userId, List<KycStatus> statuses);

    Optional<KycVerification> findFirstByProviderRef(String providerRef);
    
    @Query("SELECT k FROM KycVerification k WHERE k.status = 'VERIFIED' AND k.expiresAt > :now AND k.expiresAt <= :threshold")
    List<KycVerification> findExpiringSoon(LocalDateTime now, LocalDateTime threshold);
}
