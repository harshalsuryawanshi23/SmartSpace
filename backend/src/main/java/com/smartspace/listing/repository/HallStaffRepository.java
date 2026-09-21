package com.smartspace.listing.repository;

import com.smartspace.listing.entity.HallStaff;
import com.smartspace.listing.entity.HallStaffId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HallStaffRepository extends JpaRepository<HallStaff, HallStaffId> {
    List<HallStaff> findByIdHallId(Long hallId);
    List<HallStaff> findByIdUserId(Long userId);
}
