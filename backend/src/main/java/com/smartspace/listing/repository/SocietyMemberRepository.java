package com.smartspace.listing.repository;

import com.smartspace.listing.entity.SocietyMember;
import com.smartspace.listing.entity.SocietyMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("listingSocietyMemberRepository")
public interface SocietyMemberRepository extends JpaRepository<SocietyMember, SocietyMemberId> {
    List<SocietyMember> findBySocietyId(Long societyId);
    List<SocietyMember> findByUserId(Long userId);
}
