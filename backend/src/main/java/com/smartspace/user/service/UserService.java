package com.smartspace.user.service;

import com.smartspace.common.exception.DomainException;
import com.smartspace.common.exception.ErrorCode;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "User not found"));
    }

    @Transactional
    public void updatePreferredLanguage(String publicId, String language) {
        User user = getUserByPublicId(publicId);
        user.setPreferredLanguage(language);
        userRepository.save(user);
    }
    
    @Transactional
    public void updateProfilePhoto(String publicId, String photoPath) {
        User user = getUserByPublicId(publicId);
        user.setProfilePhotoPath(photoPath);
        userRepository.save(user);
    }
}
