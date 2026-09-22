package com.smartspace.listing.repository;

import com.smartspace.listing.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    Optional<Hall> findByPublicId(String publicId);
    List<Hall> findByOwnerUserId(Long ownerUserId);
    List<Hall> findBySocietyId(Long societyId);

    @Query(value = "SELECT h.* FROM halls h " +
            "WHERE h.status = 'ACTIVE' " +
            "AND h.lat BETWEEN :minLat AND :maxLat AND h.lng BETWEEN :minLng AND :maxLng " +
            "AND h.capacity_standing >= :guests " +
            "AND (6371 * ACOS(LEAST(1.0, COS(RADIANS(:lat)) * COS(RADIANS(h.lat)) * COS(RADIANS(h.lng) - RADIANS(:lng)) + SIN(RADIANS(:lat)) * SIN(RADIANS(h.lat))))) <= :radius " +
            "AND (:hasWindow = 0 OR NOT EXISTS ( " +
            "      SELECT 1 FROM booking_cells c JOIN bookings b ON b.id = c.booking_id " +
            "      WHERE c.hall_id = h.id AND c.cell_start >= :winStart AND c.cell_start < :winEndPlusBuffer " +
            "        AND NOT (b.status = 'PENDING_PAYMENT' AND b.lock_expires_at < :now))) " +
            "ORDER BY (6371 * ACOS(LEAST(1.0, COS(RADIANS(:lat)) * COS(RADIANS(h.lat)) * COS(RADIANS(h.lng) - RADIANS(:lng)) + SIN(RADIANS(:lat)) * SIN(RADIANS(h.lat))))) ASC " +
            "LIMIT 200", nativeQuery = true)
    List<Hall> searchHalls(
            @Param("lat") java.math.BigDecimal lat,
            @Param("lng") java.math.BigDecimal lng,
            @Param("minLat") java.math.BigDecimal minLat,
            @Param("maxLat") java.math.BigDecimal maxLat,
            @Param("minLng") java.math.BigDecimal minLng,
            @Param("maxLng") java.math.BigDecimal maxLng,
            @Param("guests") int guests,
            @Param("radius") double radius,
            @Param("hasWindow") int hasWindow,
            @Param("winStart") java.time.LocalDateTime winStart,
            @Param("winEndPlusBuffer") java.time.LocalDateTime winEndPlusBuffer,
            @Param("now") java.time.LocalDateTime now
    );
}
