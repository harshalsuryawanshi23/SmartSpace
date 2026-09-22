package com.smartspace.admin.service;

import com.smartspace.common.exception.ResourceNotFoundException;
import com.smartspace.user.entity.User;
import com.smartspace.user.entity.UserStatus;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public void suspendUser(Long adminId, Long targetUserId, String reason) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        target.setStatus(UserStatus.SUSPENDED);
        // Normally, audit log would record the 'reason' and 'adminId'
        userRepository.save(target);
    }

    @Transactional
    public void reactivateUser(Long adminId, Long targetUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        target.setStatus(UserStatus.ACTIVE);
        userRepository.save(target);
    }

    @Transactional
    public void revokeKyc(Long adminId, Long targetUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        target.setKycStatus(com.smartspace.user.entity.KycStatus.REJECTED); // Or NONE
        userRepository.save(target);
    }
}
