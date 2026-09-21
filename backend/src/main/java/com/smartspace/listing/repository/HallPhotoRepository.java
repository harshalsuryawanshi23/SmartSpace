package com.smartspace.listing.repository;

import com.smartspace.listing.entity.HallPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallPhotoRepository extends JpaRepository<HallPhoto, Long> {
    List<HallPhoto> findByHallIdOrderBySortOrderAsc(Long hallId);
}
