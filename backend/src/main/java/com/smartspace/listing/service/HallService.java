package com.smartspace.listing.service;

import com.smartspace.auth.repository.UserRepository;
import com.smartspace.listing.dto.HallCreateRequest;
import com.smartspace.listing.dto.HallDto;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallStatus;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final SocietyRepository societyRepository;
    private final UserRepository userRepository;

    @Transactional
    public HallDto createHall(Long ownerUserId, HallCreateRequest request) {
        Society society = societyRepository.findByPublicId(request.getSocietyId())
                .orElseThrow(() -> new IllegalArgumentException("Society not found"));
                
        // Validation per requirements
        if (request.getMinSlotMinutes() % 30 != 0 || request.getMaxSlotMinutes() % 30 != 0) {
            throw new IllegalArgumentException("Slot minutes must be multiples of 30");
        }
        if (request.getMinSlotMinutes() > request.getMaxSlotMinutes()) {
            throw new IllegalArgumentException("min slot cannot be > max slot");
        }
        if (request.getCapacitySeated() <= 0 || request.getCapacityStanding() < request.getCapacitySeated()) {
            throw new IllegalArgumentException("Invalid capacity logic");
        }

        Hall hall = Hall.builder()
                .societyId(society.getId())
                .ownerUserId(ownerUserId)
                .name(request.getName())
                .description(request.getDescription())
                .addressLine(request.getAddressLine())
                .locality(request.getLocality())
                .city(request.getCity())
                .lat(request.getLat())
                .lng(request.getLng())
                .capacitySeated(request.getCapacitySeated())
                .capacityStanding(request.getCapacityStanding())
                .areaSqft(request.getAreaSqft())
                .layoutType(request.getLayoutType())
                .ceilingHeightFt(request.getCeilingHeightFt())
                .indoor(request.getIndoor())
                .hasAc(request.getHasAc())
                .hasParking(request.getHasParking())
                .hasKitchen(request.getHasKitchen())
                .hasStage(request.getHasStage())
                .hasPowerBackup(request.getHasPowerBackup())
                .hasWashroom(request.getHasWashroom())
                .powerPoints(request.getPowerPoints())
                .rulesText(request.getRulesText())
                .basePricePerHour(request.getBasePricePerHour())
                .minSlotMinutes(request.getMinSlotMinutes())
                .maxSlotMinutes(request.getMaxSlotMinutes())
                .bufferAfterMinutes(request.getBufferAfterMinutes())
                .quietHoursStart(request.getQuietHoursStart())
                .quietHoursEnd(request.getQuietHoursEnd())
                .latestEndTime(request.getLatestEndTime())
                .advanceDaysPublic(request.getAdvanceDaysPublic())
                .advanceDaysMember(request.getAdvanceDaysMember())
                .memberDiscountPercent(request.getMemberDiscountPercent())
                .cancellationPolicy(request.getCancellationPolicy())
                .overstayFeePer15Min(request.getOverstayFeePer15Min())
                .status(HallStatus.DRAFT)
                .build();
                
        hall = hallRepository.save(hall);
        return mapToDto(hall);
    }

    public List<HallDto> getHallsForOwner(Long ownerUserId) {
        return hallRepository.findByOwnerUserId(ownerUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public HallDto getHall(String publicId) {
        Hall hall = hallRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
        return mapToDto(hall);
    }

    @Transactional
    public void submitHallForApproval(String publicId, Long ownerUserId) {
        Hall hall = hallRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Hall not found"));
                
        if (!hall.getOwnerUserId().equals(ownerUserId)) {
            throw new SecurityException("Not authorized to submit this hall");
        }
        
        if (hall.getStatus() != HallStatus.DRAFT && hall.getStatus() != HallStatus.REJECTED) {
            throw new IllegalStateException("Hall cannot be submitted from current state");
        }
        
        hall.setStatus(HallStatus.PENDING_APPROVAL);
        hallRepository.save(hall);
    }

    private HallDto mapToDto(Hall hall) {
        HallDto dto = new HallDto();
        dto.setId(hall.getPublicId());
        
        societyRepository.findById(hall.getSocietyId())
            .ifPresent(s -> dto.setSocietyId(s.getPublicId()));
            
        dto.setName(hall.getName());
        dto.setDescription(hall.getDescription());
        dto.setAddressLine(hall.getAddressLine());
        dto.setLocality(hall.getLocality());
        dto.setCity(hall.getCity());
        dto.setLat(hall.getLat());
        dto.setLng(hall.getLng());
        dto.setCapacitySeated(hall.getCapacitySeated());
        dto.setCapacityStanding(hall.getCapacityStanding());
        dto.setAreaSqft(hall.getAreaSqft());
        dto.setLayoutType(hall.getLayoutType());
        dto.setCeilingHeightFt(hall.getCeilingHeightFt());
        dto.setIndoor(hall.getIndoor());
        dto.setHasAc(hall.getHasAc());
        dto.setHasParking(hall.getHasParking());
        dto.setHasKitchen(hall.getHasKitchen());
        dto.setHasStage(hall.getHasStage());
        dto.setHasPowerBackup(hall.getHasPowerBackup());
        dto.setHasWashroom(hall.getHasWashroom());
        dto.setPowerPoints(hall.getPowerPoints());
        dto.setRulesText(hall.getRulesText());
        dto.setBasePricePerHour(hall.getBasePricePerHour());
        dto.setMinSlotMinutes(hall.getMinSlotMinutes());
        dto.setMaxSlotMinutes(hall.getMaxSlotMinutes());
        dto.setBufferAfterMinutes(hall.getBufferAfterMinutes());
        dto.setQuietHoursStart(hall.getQuietHoursStart());
        dto.setQuietHoursEnd(hall.getQuietHoursEnd());
        dto.setLatestEndTime(hall.getLatestEndTime());
        dto.setAdvanceDaysPublic(hall.getAdvanceDaysPublic());
        dto.setAdvanceDaysMember(hall.getAdvanceDaysMember());
        dto.setMemberDiscountPercent(hall.getMemberDiscountPercent());
        dto.setCancellationPolicy(hall.getCancellationPolicy());
        dto.setOverstayFeePer15Min(hall.getOverstayFeePer15Min());
        dto.setStatus(hall.getStatus());
        dto.setRejectionReason(hall.getRejectionReason());
        dto.setRatingAvg(hall.getRatingAvg());
        dto.setRatingCount(hall.getRatingCount());
        dto.setTrustScore(hall.getTrustScore());
        dto.setCreatedAt(hall.getCreatedAt());
        return dto;
    }
}
