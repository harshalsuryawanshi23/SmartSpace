package com.smartspace.dev;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;

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
public class DevDataSeeder implements CommandLineRunner {

    private final Flyway flyway;
    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;
    private final HallRepository hallRepository;
    private final PasswordEncoder passwordEncoder;
    private final MutableClock clock;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Seeding data automatically...");
            resetAndSeed();
        } else {
            log.info("Database already contains data. Skipping automatic seed.");
        }
    }

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
        String password = passwordEncoder.encode("Demo@12345");

        createUser("Admin", "admin@smartspace.test", "9000000001", password, UserRole.ADMIN, UserStatus.ACTIVE);
        createUser("Hall Owner", "owner.meadows@smartspace.test", "9000000002", password, UserRole.HALL_OWNER, UserStatus.ACTIVE);
        createUser("Watchman", "watch.meadows@smartspace.test", "9000000003", password, UserRole.WATCHMAN, UserStatus.ACTIVE);
        createUser("Resident Priya", "priya@smartspace.test", "9000000004", password, UserRole.RESIDENT, UserStatus.ACTIVE);
        createUser("Resident Rahul", "rahul@smartspace.test", "9000000005", password, UserRole.RESIDENT, UserStatus.PENDING);
        createUser("Rang Decor", "rang.decor@smartspace.test", "9000000006", password, UserRole.DECORATOR, UserStatus.ACTIVE);
    }

    private User createUser(String name, String email, String phone, String passwordHash, UserRole role, UserStatus status) {
        User user = new User();
        user.setPublicId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordHash);
        user.setRoles(Set.of(role));
        user.setFullName(name);
        user.setStatus(status);
        return userRepository.save(user);
    }

    private void seedSocietiesAndHalls() {
        User owner = userRepository.findByEmail("owner.meadows@smartspace.test").orElseThrow();

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
    }
}
