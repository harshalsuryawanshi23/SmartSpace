package com.smartspace.security.auth;

public class SmartSpacePrincipal {
    private final Long id;
    private final String publicId;

    public SmartSpacePrincipal(Long id, String publicId) {
        this.id = id;
        this.publicId = publicId;
    }

    public Long getId() {
        return id;
    }

    public String getPublicId() {
        return publicId;
    }

    @Override
    public String toString() {
        return publicId;
    }
}
