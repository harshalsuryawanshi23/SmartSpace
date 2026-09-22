package com.smartspace.trust.repository;

import com.smartspace.trust.entity.TrustScore;
import com.smartspace.trust.entity.TrustScoreId;
import com.smartspace.trust.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TrustScoreRepository extends JpaRepository<TrustScore, TrustScoreId> {
    
    Optional<TrustScore> findBySubjectTypeAndSubjectId(Rating.SubjectType subjectType, Long subjectId);
}
