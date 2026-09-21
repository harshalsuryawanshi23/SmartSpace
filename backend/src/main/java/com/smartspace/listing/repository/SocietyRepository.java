package com.smartspace.listing.repository;

import com.smartspace.listing.entity.Society;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyRepository extends JpaRepository<Society, Long> {
    Optional<Society> findByPublicId(String publicId);
    List<Society> findByNameContainingIgnoreCaseOrLocalityContainingIgnoreCase(String name, String locality);
    List<Society> findByManagerUserId(Long managerUserId);
}
