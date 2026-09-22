package com.smartspace.decorator.service;

import com.smartspace.common.exception.ApiError;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.decorator.repository.DecoratorPackageRepository;
import com.smartspace.decorator.repository.DecoratorRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DecoratorService {

    private final DecoratorRepository decoratorRepository;
    private final DecoratorPackageRepository packageRepository;
    private final UserRepository userRepository;

    public DecoratorService(DecoratorRepository decoratorRepository, DecoratorPackageRepository packageRepository, UserRepository userRepository) {
        this.decoratorRepository = decoratorRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    public Decorator createProfile(Long userId, Decorator request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiError("User not found"));

        if (decoratorRepository.findByUserId(userId).isPresent()) {
            throw new ApiError("Decorator profile already exists for this user");
        }

        request.setPublicId(UUID.randomUUID().toString());
        request.setUser(user);
        request.setVerificationStatus(Decorator.VerificationStatus.PENDING);
        
        return decoratorRepository.save(request);
    }

    public Decorator getProfileByUserId(Long userId) {
        return decoratorRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiError("Decorator profile not found"));
    }

    public DecoratorPackage addPackage(Long decoratorId, DecoratorPackage pkg) {
        Decorator decorator = decoratorRepository.findById(decoratorId)
                .orElseThrow(() -> new ApiError("Decorator not found"));
        
        pkg.setDecorator(decorator);
        return packageRepository.save(pkg);
    }

    public List<DecoratorPackage> getPackages(Long decoratorId) {
        return packageRepository.findByDecoratorIdAndActiveTrue(decoratorId);
    }
}
