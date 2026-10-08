package com.smartspace.dev;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartspace.user.entity.User;
import com.smartspace.user.entity.UserRole;
import com.smartspace.user.entity.UserStatus;
import com.smartspace.user.repository.UserRepository;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.repository.SocietyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.Set;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder {

    private final Flyway flyway;
    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;
    private final HallRepository hallRepository;
    private final PasswordEncoder passwordEncoder;
    private final MutableClock clock;

    @Transactional
    public void resetAndSeed() {
        log.info("Resetting database via Flyway clean/migrate...");
        flyway.clean();
        flyway.migrate();
        
        clock.reset();
        
        log.info("Seeding data...");
        seedUsers();
        seedSocietiesAndHalls();
        log.info("Demo seed data complete.");
    }

    private void seedUsers() {
        User user = new User();
        user.setPublicId(UUID.randomUUID().toString());
        user.setEmail("user@example.com");
        user.setPhone("9876543210");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRoles(Set.of(UserRole.RESIDENT));
        user.setFullName("Demo User");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        User owner = new User();
        owner.setPublicId(UUID.randomUUID().toString());
        owner.setEmail("owner@example.com");
        owner.setPhone("9876543211");
        owner.setPasswordHash(passwordEncoder.encode("password123"));
        owner.setRoles(Set.of(UserRole.HALL_OWNER));
        owner.setFullName("Demo Owner");
        owner.setStatus(UserStatus.ACTIVE);
        userRepository.save(owner);
    }

    private void seedSocietiesAndHalls() {
        User owner = userRepository.findByEmail("owner@example.com").orElseThrow();

        Society society = new Society();
        society.setManagerUserId(owner.getId());
        society.setName("Grand Royal Society");
        society.setAddressLine("123 Main Street");
        society.setCity("Mumbai");
        society.setLocality("Andheri West");
        society.setPincode("400053");
        society.setLat(new BigDecimal("19.1197"));
        society.setLng(new BigDecimal("72.8464"));
        society.setVerificationStatus(com.smartspace.listing.entity.VerificationStatus.APPROVED);
        society = societyRepository.save(society);

        Hall hall = new Hall();
        hall.setSocietyId(society.getId());
        hall.setOwnerUserId(owner.getId());
        hall.setName("Crystal Banquet Hall");
        hall.setDescription("A beautiful, fully air-conditioned hall suitable for weddings and receptions.");
        hall.setCapacitySeated(200);
        hall.setCapacityStanding(400);
        hall.setAreaSqft(5000);
        hall.setHasAc(true);
        hall.setHasParking(true);
        hall.setHasKitchen(true);
        hall.setHasStage(true);
        hall.setHasPowerBackup(true);
        hall.setHasWashroom(true);
        hall.setBasePricePerHour(new BigDecimal("5000"));
        hall.setMinSlotMinutes(240);
        hall.setAddressLine("123 Main Street");
        hall.setLocality("Andheri West");
        hall.setCity("Mumbai");
        hall.setLat(new BigDecimal("19.1197"));
        hall.setLng(new BigDecimal("72.8464"));
        hallRepository.save(hall);
        
        Hall hall2 = new Hall();
        hall2.setSocietyId(society.getId());
        hall2.setOwnerUserId(owner.getId());
        hall2.setName("Silver Mini Hall");
        hall2.setDescription("A compact hall for birthdays and small gatherings.");
        hall2.setCapacitySeated(50);
        hall2.setCapacityStanding(100);
        hall2.setAreaSqft(1500);
        hall2.setHasAc(true);
        hall2.setHasParking(false);
        hall2.setHasKitchen(false);
        hall2.setHasStage(false);
        hall2.setHasPowerBackup(true);
        hall2.setHasWashroom(true);
        hall2.setBasePricePerHour(new BigDecimal("1500"));
        hall2.setMinSlotMinutes(180);
        hall2.setAddressLine("123 Main Street");
        hall2.setLocality("Andheri West");
        hall2.setCity("Mumbai");
        hall2.setLat(new BigDecimal("19.1197"));
        hall2.setLng(new BigDecimal("72.8464"));
        hallRepository.save(hall2);
    }
}
