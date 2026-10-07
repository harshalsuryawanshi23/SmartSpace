package com.smartspace.security.util;

/**
 * Re-export of SecurityUtils for classes that import from this package path.
 * @see com.smartspace.security.auth.SecurityUtils
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getCurrentUserId() {
        return com.smartspace.security.auth.SecurityUtils.getCurrentUserId();
    }

    public static String getCurrentUserPublicId() {
        return com.smartspace.security.auth.SecurityUtils.getCurrentUserPublicId();
    }
}
