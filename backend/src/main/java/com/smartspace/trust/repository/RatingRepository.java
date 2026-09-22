package com.smartspace.trust.repository;

import com.smartspace.trust.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    Optional<Rating> findByBookingIdAndRaterSideAndSubjectType(Long bookingId, Rating.RaterSide raterSide, Rating.SubjectType subjectType);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.raterUserId = :raterUserId AND r.subjectId = :subjectId AND r.subjectType = :subjectType AND r.createdAt >= :since")
    long countRatingsByRaterAndSubjectSince(@Param("raterUserId") Long raterUserId, 
                                            @Param("subjectId") Long subjectId, 
                                            @Param("subjectType") Rating.SubjectType subjectType, 
                                            @Param("since") LocalDateTime since);

    List<Rating> findBySubjectTypeAndSubjectIdOrderByCreatedAtDesc(Rating.SubjectType subjectType, Long subjectId);
    
    List<Rating> findByBookingId(Long bookingId);
}
