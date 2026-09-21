package com.smartspace.listing.service;

import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallStatus;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.entity.VerificationStatus;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApprovalService {

    private final SocietyRepository societyRepository;
    private final HallRepository hallRepository;

    public List<Society> getPendingSocieties() {
        return societyRepository.findAll().stream()
                .filter(s -> s.getVerificationStatus() == VerificationStatus.PENDING)
                .toList();
    }

    public List<Hall> getPendingHalls() {
        return hallRepository.findAll().stream()
                .filter(h -> h.getStatus() == HallStatus.PENDING_APPROVAL)
                .toList();
    }

    @Transactional
    public void reviewSociety(String publicId, boolean approve, String reason) {
        Society society = societyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Society not found"));
                
        society.setVerificationStatus(approve ? VerificationStatus.APPROVED : VerificationStatus.REJECTED);
        society.setRejectionReason(approve ? null : reason);
        societyRepository.save(society);
        
        // In a real app, emit notification and audit log here.
    }

    @Transactional
    public void reviewHall(String publicId, boolean approve, String reason) {
        Hall hall = hallRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        hall.setStatus(approve ? HallStatus.ACTIVE : HallStatus.REJECTED);
        hall.setRejectionReason(approve ? null : reason);
        hallRepository.save(hall);
        
        // In a real app, emit notification and audit log here.
    }
}
