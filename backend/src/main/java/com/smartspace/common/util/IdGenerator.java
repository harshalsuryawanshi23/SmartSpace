package com.smartspace.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Predicate;

@Component
public class IdGenerator {

    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluded I, O, 1, 0

    public IdGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generatePublicId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a collision-safe booking reference in the format SS-YYMM-XXXXX
     * @param isUnique A predicate that checks if the generated ref is unique in the DB
     * @return A unique booking ref
     * @throws IllegalStateException if a unique ref cannot be generated after 3 attempts
     */
    public String generateBookingRef(Predicate<String> isUnique) {
        String prefix = "SS-" + LocalDate.now(clock).format(DateTimeFormatter.ofPattern("yyMM")) + "-";
        int attempts = 0;
        
        while (attempts < 3) {
            String ref = prefix + generateRandomString(5);
            if (isUnique.test(ref)) {
                return ref;
            }
            attempts++;
        }
        
        throw new IllegalStateException("Failed to generate a unique booking ref after 3 attempts");
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
