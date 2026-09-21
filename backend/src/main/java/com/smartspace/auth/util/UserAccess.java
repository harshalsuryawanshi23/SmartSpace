package com.smartspace.auth.util;

import com.smartspace.common.exception.DomainException;
import com.smartspace.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("userAccess")
public class UserAccess {

    public boolean isCurrentUser(String publicId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getName().equals(publicId);
    }
    
    public void requireCurrentUser(String publicId) {
        if (!isCurrentUser(publicId)) {
            // As per architecture rules, we return 404 (NOT_FOUND) instead of 403 to prevent enumeration
            throw new DomainException(ErrorCode.NOT_FOUND, "Resource not found");
        }
    }
}
