package com.smartspace.listing.service;

import com.smartspace.auth.entity.User;
import com.smartspace.auth.repository.UserRepository;
import com.smartspace.listing.dto.SocietyCreateRequest;
import com.smartspace.listing.dto.SocietyDto;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.entity.SocietyMember;
import com.smartspace.listing.entity.SocietyMemberId;
import com.smartspace.listing.entity.SocietyMemberStatus;
import com.smartspace.listing.entity.VerificationStatus;
import com.smartspace.listing.repository.SocietyMemberRepository;
import com.smartspace.listing.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocietyService {

    private final SocietyRepository societyRepository;
    private final SocietyMemberRepository societyMemberRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public SocietyDto createSociety(Long managerUserId, SocietyCreateRequest request) {
        Society society = Society.builder()
                .name(request.getName())
                .addressLine(request.getAddressLine())
                .locality(request.getLocality())
                .city(request.getCity())
                .pincode(request.getPincode())
                .lat(request.getLat())
                .lng(request.getLng())
                .managerUserId(managerUserId)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        
        society = societyRepository.save(society);
        return mapToDto(society);
    }

    public List<SocietyDto> getSocietiesForManager(Long managerUserId) {
        return societyRepository.findByManagerUserId(managerUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SocietyDto> searchSocieties(String query) {
        return societyRepository.findByNameContainingIgnoreCaseOrLocalityContainingIgnoreCase(query, query).stream()
                .filter(s -> s.getVerificationStatus() == VerificationStatus.APPROVED)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void requestJoin(String societyPublicId, Long userId, String flatLabel) {
        Society society = societyRepository.findByPublicId(societyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Society not found"));
        
        SocietyMemberId id = new SocietyMemberId(society.getId(), userId);
        if (societyMemberRepository.existsById(id)) {
            throw new IllegalStateException("Join request already exists");
        }
        
        SocietyMember member = SocietyMember.builder()
                .id(id)
                .flatLabel(flatLabel)
                .status(SocietyMemberStatus.PENDING)
                .build();
        
        societyMemberRepository.save(member);
    }

    @Transactional
    public void approveMember(String societyPublicId, Long managerUserId, String memberPublicId) {
        Society society = societyRepository.findByPublicId(societyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Society not found"));
                
        if (!society.getManagerUserId().equals(managerUserId)) {
            throw new SecurityException("Not authorized to manage this society");
        }
        
        User user = userRepository.findByPublicId(memberPublicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        SocietyMemberId id = new SocietyMemberId(society.getId(), user.getId());
        SocietyMember member = societyMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membership request not found"));
                
        member.setStatus(SocietyMemberStatus.APPROVED);
        member.setApprovedAt(LocalDateTime.now(clock));
        societyMemberRepository.save(member);
    }

    private SocietyDto mapToDto(Society entity) {
        SocietyDto dto = new SocietyDto();
        dto.setId(entity.getPublicId());
        dto.setName(entity.getName());
        dto.setAddressLine(entity.getAddressLine());
        dto.setLocality(entity.getLocality());
        dto.setCity(entity.getCity());
        dto.setPincode(entity.getPincode());
        dto.setLat(entity.getLat());
        dto.setLng(entity.getLng());
        dto.setVerificationStatus(entity.getVerificationStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
