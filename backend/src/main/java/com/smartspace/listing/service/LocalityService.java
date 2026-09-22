package com.smartspace.listing.service;

import com.smartspace.listing.entity.Locality;
import com.smartspace.listing.repository.LocalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalityService {
    private final LocalityRepository localityRepository;

    @Transactional(readOnly = true)
    public List<Locality> searchLocalities(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return localityRepository.findByNameContainingIgnoreCase(query.trim());
    }
}
