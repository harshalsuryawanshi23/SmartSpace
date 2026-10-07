package com.smartspace.listing.service;

import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import com.smartspace.listing.dto.HallCreateRequest;
import com.smartspace.listing.dto.HallDto;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.LayoutType;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.SocietyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;
    @Mock
    private SocietyRepository societyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HallService hallService;

    @Test
    void createHall_success() {
        Society society = new Society();
        society.setId(1L);
        society.setPublicId("soc-pub-id");
        when(societyRepository.findByPublicId("soc-pub-id")).thenReturn(Optional.of(society));
        
        Hall savedHall = new Hall();
        savedHall.setId(10L);
        savedHall.setPublicId("hall-pub-id");
        savedHall.setSocietyId(1L);
        savedHall.setOwnerUserId(5L);
        
        when(hallRepository.save(any(Hall.class))).thenReturn(savedHall);

        HallCreateRequest req = new HallCreateRequest();
        req.setSocietyId("soc-pub-id");
        req.setName("Test Hall");
        req.setMinSlotMinutes(120);
        req.setMaxSlotMinutes(240);
        req.setCapacitySeated(100);
        req.setCapacityStanding(150);
        req.setLayoutType(LayoutType.OPEN_HALL);
        
        HallDto result = hallService.createHall(5L, req);
        
        assertNotNull(result);
        assertEquals("hall-pub-id", result.getId());
        verify(hallRepository, times(1)).save(any(Hall.class));
    }

    @Test
    void createHall_validationFailure() {
        Society society = new Society();
        society.setId(1L);
        when(societyRepository.findByPublicId("soc-pub-id")).thenReturn(Optional.of(society));

        HallCreateRequest req = new HallCreateRequest();
        req.setSocietyId("soc-pub-id");
        req.setMinSlotMinutes(45); // Not a multiple of 30
        req.setMaxSlotMinutes(120);
        
        assertThrows(IllegalArgumentException.class, () -> hallService.createHall(5L, req));
    }
}
