package com.smartspace.security.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to extract the current authenticated user from the Spring Security context.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the current authenticated user's internal database ID.
     * Requires that the Authentication principal stores a user ID (Long).
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof SmartSpacePrincipal) {
            return ((SmartSpacePrincipal) principal).getId();
        }
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Cannot extract numeric user ID from principal: " + principal);
            }
        }
        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }

    public static String getCurrentUserPublicId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof SmartSpacePrincipal) {
            return ((SmartSpacePrincipal) principal).getPublicId();
        }
        return principal.toString();
    }
}
