package com.smartspace.listing.repository;

import com.smartspace.listing.entity.Locality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalityRepository extends JpaRepository<Locality, Long> {
    List<Locality> findByNameContainingIgnoreCase(String name);
}
