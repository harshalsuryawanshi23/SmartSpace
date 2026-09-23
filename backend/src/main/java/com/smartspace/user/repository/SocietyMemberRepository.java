package com.smartspace.user.repository;

import com.smartspace.user.entity.SocietyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocietyMemberRepository extends JpaRepository<SocietyMember, Long> {
    Optional<SocietyMember> findByUserIdAndHallId(Long userId, Long hallId);
}
