package com.smartspace.auth.service;

import com.smartspace.security.auth.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * Service that provides the current authenticated user's context.
 * Wraps SecurityUtils for dependency injection.
 */
@Service
public class AuthContextService {

    /**
     * Returns the current authenticated user's database ID.
     */
    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    /**
     * Returns the current authenticated user's public ID.
     */
    public String getCurrentUserPublicId() {
        return SecurityUtils.getCurrentUserPublicId();
    }
}
