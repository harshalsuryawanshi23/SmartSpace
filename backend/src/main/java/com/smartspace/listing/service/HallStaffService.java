package com.smartspace.listing.service;

import com.smartspace.auth.entity.User;
import com.smartspace.auth.repository.UserRepository;
import com.smartspace.listing.dto.HallStaffDto;
import com.smartspace.listing.dto.HallStaffRequest;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallStaff;
import com.smartspace.listing.entity.HallStaffId;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.HallStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallStaffService {

    private final HallStaffRepository hallStaffRepository;
    private final HallRepository hallRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public HallStaffDto addStaff(String hallPublicId, Long ownerUserId, HallStaffRequest request) {
        Hall hall = hallRepository.findByPublicId(hallPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized");
        }
        
        // Find user by email or phone. If not exists, create a basic user.
        // For brevity in MVP, assuming user is created if not exists.
        User user = userRepository.findByEmail(request.getEmail()).orElseGet(() -> {
            User newUser = User.builder()
                    .publicId(java.util.UUID.randomUUID().toString())
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .passwordHash(passwordEncoder.encode(request.getTempPassword()))
                    .status(com.smartspace.auth.entity.UserStatus.ACTIVE)
                    .preferredLanguage("en")
                    .build();
            return userRepository.save(newUser);
        });

        HallStaffId id = new HallStaffId(hall.getId(), user.getId());
        HallStaff staff = HallStaff.builder()
                .id(id)
                .staffRole(request.getRole())
                .active(true)
                .build();
                
        hallStaffRepository.save(staff);
        
        HallStaffDto dto = new HallStaffDto();
        dto.setUserId(user.getPublicId());
        dto.setRole(staff.getStaffRole());
        dto.setActive(staff.getActive());
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }

    public List<HallStaffDto> getStaff(String hallPublicId, Long ownerUserId) {
        Hall hall = hallRepository.findByPublicId(hallPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized");
        }
        
        return hallStaffRepository.findByIdHallId(hall.getId()).stream().map(s -> {
            HallStaffDto dto = new HallStaffDto();
            userRepository.findById(s.getId().getUserId()).ifPresent(u -> dto.setUserId(u.getPublicId()));
            dto.setRole(s.getStaffRole());
            dto.setActive(s.getActive());
            dto.setCreatedAt(s.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void removeStaff(String hallPublicId, Long ownerUserId, String staffPublicId) {
        Hall hall = hallRepository.findByPublicId(hallPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized");
        }
        
        User user = userRepository.findByPublicId(staffPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));
                
        hallStaffRepository.deleteById(new HallStaffId(hall.getId(), user.getId()));
    }
}
