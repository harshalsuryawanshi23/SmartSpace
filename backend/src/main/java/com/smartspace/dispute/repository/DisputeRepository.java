package com.smartspace.dispute.repository;

import com.smartspace.dispute.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.booking.renter.id = :renterId AND d.againstSide = 'RENTER' AND d.status = 'RESOLVED_FOR_RAISER'")
    long countUpheldDisputesAgainstRenter(@Param("renterId") Long renterId);

    java.util.List<Dispute> findByRaisedBy(Long raisedBy);
}