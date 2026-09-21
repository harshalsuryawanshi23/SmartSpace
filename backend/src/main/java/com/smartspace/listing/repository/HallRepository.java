package com.smartspace.listing.repository;

import com.smartspace.listing.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    Optional<Hall> findByPublicId(String publicId);
    List<Hall> findByOwnerUserId(Long ownerUserId);
    List<Hall> findBySocietyId(Long societyId);
}
